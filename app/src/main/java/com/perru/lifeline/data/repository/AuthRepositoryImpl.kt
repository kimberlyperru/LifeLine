package com.perru.lifeline.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.perru.lifeline.domain.model.LifeLineUser
import com.perru.lifeline.domain.model.UserRole
import com.perru.lifeline.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val USERS_NODE = "users"

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : AuthRepository {

    private val usersRef get() = database.getReference(USERS_NODE)

    override val isSignedIn: Boolean
        get() = auth.currentUser != null

    override fun currentUserFlow(): Flow<LifeLineUser?> {
        val authStateFlow = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { fa ->
                trySend(fa.currentUser?.uid)
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }.distinctUntilChanged()

        return authStateFlow.flatMapLatest { uid ->
            if (uid == null) flowOf(null) else userProfileFlow(uid)
        }
    }

    private fun userProfileFlow(uid: String): Flow<LifeLineUser?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(LifeLineUser::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        usersRef.child(uid).addValueEventListener(listener)
        awaitClose { usersRef.child(uid).removeEventListener(listener) }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<String> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("No UID returned from Firebase")
        // Seed a minimal profile node; role/details are filled in during onboarding.
        usersRef.child(uid).setValue(LifeLineUser(uid = uid, email = email, role = UserRole.UNSET)).await()
        Result.success(uid)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user?.uid ?: error("No UID returned from Firebase"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val uid = result.user?.uid ?: error("No UID returned from Firebase")
        val existing = usersRef.child(uid).get().await()
        if (!existing.exists()) {
            usersRef.child(uid).setValue(
                LifeLineUser(
                    uid = uid,
                    email = result.user?.email.orEmpty(),
                    displayName = result.user?.displayName.orEmpty(),
                    role = UserRole.UNSET
                )
            ).await()
        }
        Result.success(uid)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun setUserRole(uid: String, role: UserRole): Result<Unit> = try {
        usersRef.child(uid).child("role").setValue(role.name).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun completeOnboarding(user: LifeLineUser): Result<Unit> = try {
        usersRef.child(user.uid).setValue(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserProfile(uid: String): Result<LifeLineUser?> = try {
        val snapshot = usersRef.child(uid).get().await()
        Result.success(snapshot.getValue(LifeLineUser::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun signOut() {
        auth.signOut()
    }
}
