package com.perru.lifeline.domain.repository

import com.perru.lifeline.domain.model.LifeLineUser
import com.perru.lifeline.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Emits the current signed-in user's profile, or null when signed out. */
    fun currentUserFlow(): Flow<LifeLineUser?>

    val isSignedIn: Boolean

    suspend fun signUpWithEmail(email: String, password: String): Result<String>

    suspend fun signInWithEmail(email: String, password: String): Result<String>

    suspend fun signInWithGoogle(idToken: String): Result<String>

    suspend fun setUserRole(uid: String, role: UserRole): Result<Unit>

    suspend fun completeOnboarding(user: LifeLineUser): Result<Unit>

    suspend fun getUserProfile(uid: String): Result<LifeLineUser?>

    fun signOut()
}
