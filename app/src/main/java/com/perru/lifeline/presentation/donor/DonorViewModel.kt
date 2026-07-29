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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DonorFeedUiState(
    val allRequests: List<BloodRequest> = emptyList(),
    val compatibleOnly: Boolean = true,
    val currentUser: LifeLineUser? = null
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
        requestRepository.activeRequestsFlow(),
        authRepository.currentUserFlow(),
        _compatibleOnly
    ) { requests, user, compatibleOnly ->
        DonorFeedUiState(allRequests = requests, compatibleOnly = compatibleOnly, currentUser = user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DonorFeedUiState())

    fun toggleCompatibleOnly() {
        _compatibleOnly.value = !_compatibleOnly.value
    }

    /** Resets role to UNSET so the auth-driven nav graph routes back through onboarding,
     *  letting the person pick Donor or Hospital again. */
    fun switchRole(uid: String) {
        viewModelScope.launch {
            authRepository.setUserRole(uid, com.perru.lifeline.domain.model.UserRole.UNSET)
        }
    }

    fun pledge(request: BloodRequest, donor: LifeLineUser, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val pledge = Pledge(
                requestId = request.id,
                donorUid = donor.uid,
                donorName = donor.displayName,
                donorBloodGroup = donor.bloodGroup ?: request.bloodGroup
            )
            onResult(requestRepository.pledgeToRequest(pledge))
        }
    }
}
