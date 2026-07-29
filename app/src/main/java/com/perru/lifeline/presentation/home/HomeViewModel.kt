package com.perru.lifeline.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.LifeLineUser
import com.perru.lifeline.domain.repository.AuthRepository
import com.perru.lifeline.domain.repository.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val requests: List<BloodRequest> = emptyList(),
    val currentUser: LifeLineUser? = null
)

/**
 * Backs the public home feed. Deliberately independent of DonorViewModel:
 * Home is browsable by anyone (signed in or not), so it only ever reads —
 * pledging, role switching, etc. live on the authenticated dashboards.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    requestRepository: RequestRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        requestRepository.activeRequestsFlow(),
        authRepository.currentUserFlow()
    ) { requests, user ->
        HomeUiState(requests = requests.sortedByDescending { it.createdAtMillis }, currentUser = user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
