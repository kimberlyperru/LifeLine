package com.perru.lifeline.domain.repository

import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.Pledge
import kotlinx.coroutines.flow.Flow

interface RequestRepository {

    /** Real-time stream of active requests, newest first. */
    fun activeRequestsFlow(): Flow<List<BloodRequest>>

    suspend fun getRequest(requestId: String): Result<BloodRequest?>

    suspend fun createRequest(request: BloodRequest): Result<String>

    suspend fun pledgeToRequest(pledge: Pledge): Result<Unit>

    fun pledgesForRequestFlow(requestId: String): Flow<List<Pledge>>

    /** Requests created by a specific hospital account, for their management dashboard. */
    fun requestsByHospitalFlow(hospitalUid: String): Flow<List<BloodRequest>>
}
