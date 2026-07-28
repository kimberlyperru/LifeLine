package com.perru.lifeline.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.perru.LifeLine.domain.model.BloodRequest
import com.perru.LifeLine.domain.model.Pledge
import com.perru.LifeLine.domain.model.RequestStatus
import com.perru.LifeLine.domain.repository.RequestRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.java

private const val REQUESTS_COLLECTION = "requests"
private const val PLEDGES_COLLECTION = "pledges"

@Singleton
class RequestRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RequestRepository {

    override fun activeRequestsFlow(): Flow<List<BloodRequest>> = callbackFlow {
        val registration = firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("status", RequestStatus.ACTIVE.name)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val requests = snapshot?.documents?.mapNotNull {
                    it.toObject(BloodRequest::class.java)?.copy(id = it.id)
                }.orEmpty()
                trySend(requests)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getRequest(requestId: String): Result<BloodRequest?> = try {
        val doc = firestore.collection(REQUESTS_COLLECTION).document(requestId).get().await()
        Result.success(doc.toObject(BloodRequest::class.java)?.copy(id = doc.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createRequest(request: BloodRequest): Result<String> = try {
        val docRef = firestore.collection(REQUESTS_COLLECTION).document()
        docRef.set(request.copy(id = docRef.id)).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun pledgeToRequest(pledge: Pledge): Result<Unit> = try {
        firestore.runTransaction { transaction ->
            val requestRef = firestore.collection(REQUESTS_COLLECTION).document(pledge.requestId)
            val snapshot = transaction.get(requestRef)
            val current = snapshot.toObject(BloodRequest::class.java) ?: error("Request not found")

            val pledgeRef = firestore.collection(PLEDGES_COLLECTION).document()
            transaction.set(pledgeRef, pledge.copy(id = pledgeRef.id))

            val newPledgedUnits = current.unitsPledged + 1
            val newStatus = if (newPledgedUnits >= current.unitsNeeded) {
                RequestStatus.FULFILLED.name
            } else {
                current.status.name
            }
            transaction.update(
                requestRef,
                mapOf("unitsPledged" to newPledgedUnits, "status" to newStatus)
            )
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun pledgesForRequestFlow(requestId: String): Flow<List<Pledge>> = callbackFlow {
        val registration = firestore.collection(PLEDGES_COLLECTION)
            .whereEqualTo("requestId", requestId)
            .addSnapshotListener { snapshot, _ ->
                val pledges = snapshot?.documents?.mapNotNull {
                    it.toObject(Pledge::class.java)?.copy(id = it.id)
                }.orEmpty()
                trySend(pledges)
            }
        awaitClose { registration.remove() }
    }

    override fun requestsByHospitalFlow(hospitalUid: String): Flow<List<BloodRequest>> = callbackFlow {
        val registration = firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("hospitalUid", hospitalUid)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val requests = snapshot?.documents?.mapNotNull {
                    it.toObject(BloodRequest::class.java)?.copy(id = it.id)
                }.orEmpty()
                trySend(requests)
            }
        awaitClose { registration.remove() }
    }
}