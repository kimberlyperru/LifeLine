package com.perru.lifeline.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.Pledge
import com.perru.lifeline.domain.model.RequestStatus
import com.perru.lifeline.domain.repository.RequestRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Design note: pledges live in their own top-level node (not nested under
// requests) so both "pledges for a request" and "a donor's pledge history"
// can be queried directly without walking the whole request tree.
private const val REQUESTS_NODE = "requests"
private const val PLEDGES_NODE = "pledges"

@Singleton
class RequestRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : RequestRepository {

    private val requestsRef get() = database.getReference(REQUESTS_NODE)
    private val pledgesRef get() = database.getReference(PLEDGES_NODE)

    override fun activeRequestsFlow(): Flow<List<BloodRequest>> =
        requestsRef.observeAsList<BloodRequest> { request, id -> request.copy(id = id) }
            .map { requests ->
                requests.filter { it.status == RequestStatus.ACTIVE }
                    .sortedByDescending { it.createdAtMillis }
            }

    override suspend fun getRequest(requestId: String): Result<BloodRequest?> = try {
        val snapshot = requestsRef.child(requestId).get().await()
        Result.success(snapshot.getValue(BloodRequest::class.java)?.copy(id = snapshot.key.orEmpty()))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createRequest(request: BloodRequest): Result<String> = try {
        val newRef = requestsRef.push()
        val id = newRef.key ?: error("Could not generate a request ID")
        newRef.setValue(request.copy(id = id)).await()
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun pledgeToRequest(pledge: Pledge): Result<Unit> = try {
        // Atomically increment unitsPledged (and flip status if now fulfilled) using
        // a Realtime Database transaction — this safely resolves concurrent pledges,
        // the RTDB equivalent of a Firestore transactional read-modify-write.
        runTransactionOnRequest(pledge.requestId)
        val pledgeRef = pledgesRef.push()
        val id = pledgeRef.key ?: error("Could not generate a pledge ID")
        pledgeRef.setValue(pledge.copy(id = id)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun runTransactionOnRequest(requestId: String): Unit = suspendCancellableCoroutine { cont ->
        requestsRef.child(requestId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = currentData.getValue(BloodRequest::class.java)
                    ?: return Transaction.success(currentData)
                val newPledgedUnits = current.unitsPledged + 1
                currentData.child("unitsPledged").value = newPledgedUnits
                if (newPledgedUnits >= current.unitsNeeded) {
                    currentData.child("status").value = RequestStatus.FULFILLED.name
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    cont.resumeWith(Result.failure(error.toException()))
                } else {
                    cont.resumeWith(Result.success(Unit))
                }
            }
        })
    }

    override fun pledgesForRequestFlow(requestId: String): Flow<List<Pledge>> =
        pledgesRef.observeAsList<Pledge> { pledge, id -> pledge.copy(id = id) }
            .map { pledges -> pledges.filter { it.requestId == requestId } }

    override fun requestsByHospitalFlow(hospitalUid: String): Flow<List<BloodRequest>> =
        requestsRef.observeAsList<BloodRequest> { request, id -> request.copy(id = id) }
            .map { requests ->
                requests.filter { it.hospitalUid == hospitalUid }
                    .sortedByDescending { it.createdAtMillis }
            }
}

/**
 * Listens to an entire RTDB node and maps every child to [T], using [stampId]
 * to attach each item's Firebase key as its `id` field. Filtering/sorting per
 * query happens client-side afterwards, since RTDB — unlike Firestore — can't
 * combine an equality filter with a different sort field in one query.
 */
private inline fun <reified T> DatabaseReference.observeAsList(
    crossinline stampId: (T, String) -> T
): Flow<List<T>> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val items = snapshot.children.mapNotNull { child ->
                child.getValue(T::class.java)?.let { value -> stampId(value, child.key.orEmpty()) }
            }
            trySend(items)
        }
        override fun onCancelled(error: DatabaseError) {
            trySend(emptyList())
        }
    }
    addValueEventListener(listener)
    awaitClose { removeEventListener(listener) }
}
