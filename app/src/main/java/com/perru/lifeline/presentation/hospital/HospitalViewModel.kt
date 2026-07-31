package com.perru.lifeline.presentation.hospital

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perru.lifeline.data.remote.CloudinaryUploader
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.LifeLineUser
import com.perru.lifeline.domain.model.RequestStatus
import com.perru.lifeline.domain.model.UrgencyLevel
import com.perru.lifeline.domain.model.UserRole
import com.perru.lifeline.domain.repository.AuthRepository
import com.perru.lifeline.domain.repository.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateRequestState(
    val isUploading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val submitted: Boolean = false
)

@HiltViewModel
class HospitalViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryUploader: CloudinaryUploader
) : ViewModel() {

    val currentUser: StateFlow<LifeLineUser?> = authRepository.currentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val myRequests: StateFlow<List<BloodRequest>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else requestRepository.requestsByHospitalFlow(user.uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPledges: StateFlow<Int> = myRequests.map { requests ->
        requests.filter { it.status == RequestStatus.ACTIVE }.sumOf { it.unitsPledged }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val livesSaved: StateFlow<Int> = myRequests.map { requests ->
        requests.filter { it.status == RequestStatus.FULFILLED }.sumOf { it.unitsPledged }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _createState = MutableStateFlow(CreateRequestState())
    val createState: StateFlow<CreateRequestState> = _createState.asStateFlow()

    private val _crisisMode = MutableStateFlow(false)
    val crisisMode: StateFlow<Boolean> = _crisisMode.asStateFlow()

    fun toggleCrisisMode() {
        _crisisMode.value = !_crisisMode.value
    }

    fun submitRequest(
        hospital: LifeLineUser,
        request: BloodRequest,
        verificationImageUri: Uri?
    ) {
        _createState.value = CreateRequestState(isSubmitting = true)
        viewModelScope.launch {
            var imageUrl = ""
            if (verificationImageUri != null) {
                _createState.value = _createState.value.copy(isUploading = true)
                val uploadResult = cloudinaryUploader.uploadImage(verificationImageUri)
                uploadResult.onFailure {
                    _createState.value = CreateRequestState(errorMessage = "Image upload failed: ${it.message}")
                    return@launch
                }
                imageUrl = uploadResult.getOrDefault("")
                _createState.value = _createState.value.copy(isUploading = false)
            }

            val finalRequest = request.copy(
                hospitalUid = hospital.uid,
                hospitalName = hospital.hospitalName.ifBlank { hospital.displayName },
                hospitalCity = hospital.city,
                verificationImageUrl = imageUrl,
                urgency = if (_crisisMode.value) UrgencyLevel.CRITICAL else request.urgency
            )

            requestRepository.createRequest(finalRequest)
                .onSuccess { _createState.value = CreateRequestState(submitted = true) }
                .onFailure { _createState.value = CreateRequestState(errorMessage = it.message) }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateRequestState()
    }

    /** Resets role to UNSET so the auth-driven nav graph routes back through onboarding,
     *  letting the person pick Donor or Hospital again. */
    fun switchRole(uid: String) {
        viewModelScope.launch {
            authRepository.setUserRole(uid, UserRole.UNSET)
        }
    }

    fun signOut() = authRepository.signOut()
}
