package com.perru.LifeLine.domain.repository

import com.perru.LifeLine.domain.model.BloodRequest
import com.perru.LifeLine.domain.model.Pledge
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    fun activeRequestsFlow(): Flow<List<BloodRequest>>
    suspend fun getRequest(requestId: String): Result<BloodRequest?>
    suspend fun createRequest(request: BloodRequest): Result<String>
    suspend fun pledgeToRequest(pledge: Pledge): Result<Unit>
    fun pledgesForRequestFlow(requestId: String): Flow<List<Pledge>>
    fun requestsByHospitalFlow(hospitalUid: String): Flow<List<BloodRequest>>
}