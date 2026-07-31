package com.perru.lifeline.presentation.donor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.LifeLineUser
import com.perru.lifeline.domain.model.Pledge
import com.perru.lifeline.domain.repository.AuthRepository
import com.perru.lifeline.domain.repository.RequestRepository
import com.perru.lifeline.util.BloodCompatibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DonorFeedUiState(
    val allRequests: List<BloodRequest> = emptyList(),
    val compatibleOnly: Boolean = true,
    val currentUser: LifeLineUser? = null,
    val isLoading: Boolean = false
) {
    val visibleRequests: List<BloodRequest>
        get() {
            val donorGroup = currentUser?.bloodGroup ?: return allRequests
            return if (compatibleOnly) {
                allRequests.filter { BloodCompatibility.isCompatible(donorGroup, it.bloodGroup) }
            } else {
                allRequests
            }
        }
}

@HiltViewModel
class DonorViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _compatibleOnly = MutableStateFlow(true)

    val uiState: StateFlow<DonorFeedUiState> = combine(
        requestRepository.activeRequestsFlow().onStart { /* Optionally trigger loading if repo is slow */ },
        authRepository.currentUserFlow(),
        _compatibleOnly
    ) { requests, user, compatibleOnly ->
        DonorFeedUiState(
            allRequests = requests,
            compatibleOnly = compatibleOnly,
            currentUser = user,
            isLoading = false // It's not loading once we have data from flows
        )
    }.onStart {
        emit(DonorFeedUiState(isLoading = true))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DonorFeedUiState(isLoading = true))

    fun toggleCompatibleOnly() {
        _compatibleOnly.value = !_compatibleOnly.value
    }

    fun getRequestById(id: String): Flow<BloodRequest?> {
        return requestRepository.getRequestFlow(id)
    }

    /** Resets role to UNSET so the auth-driven nav graph routes back through onboarding,
     *  letting the person pick Donor or Hospital again. */
    fun switchRole(uid: String) {
        viewModelScope.launch {
            authRepository.setUserRole(uid, com.perru.lifeline.domain.model.UserRole.UNSET)
        }
    }

    fun pledge(request: BloodRequest, donor: LifeLineUser, onResult: (Result<Unit>) -> Unit) {
        val donorGroup = donor.bloodGroup
        if (donorGroup == null || !BloodCompatibility.isCompatible(donorGroup, request.bloodGroup)) {
            onResult(Result.failure(IllegalStateException("Incompatible blood type for this request.")))
            return
        }

        viewModelScope.launch {
            val pledge = Pledge(
                requestId = request.id,
                donorUid = donor.uid,
                donorName = donor.displayName,
                donorBloodGroup = donorGroup
            )
            onResult(requestRepository.pledgeToRequest(pledge))
        }
    }

    fun signOut() = authRepository.signOut()
}
