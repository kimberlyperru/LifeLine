package com.perru.lifeline.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.perru.lifeline.domain.model.BloodGroup
import com.perru.lifeline.domain.model.LifeLineUser
import com.perru.lifeline.domain.model.UserRole
import com.perru.lifeline.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class OnboardingFormState(
    val displayName: String = "",
    val city: String = "",
    val bloodGroup: BloodGroup? = null,
    val hospitalName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<LifeLineUser?> = authRepository.currentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _loginState = MutableStateFlow(AuthFormState())
    val loginState: StateFlow<AuthFormState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(AuthFormState())
    val signUpState: StateFlow<AuthFormState> = _signUpState.asStateFlow()

    private val _onboardingState = MutableStateFlow(OnboardingFormState())
    val onboardingState: StateFlow<OnboardingFormState> = _onboardingState.asStateFlow()

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        _loginState.value = _loginState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            result.onSuccess {
                _loginState.value = AuthFormState()
                onSuccess()
            }.onFailure {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Sign in failed"
                )
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        _signUpState.value = _signUpState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, password)
            result.onSuccess {
                _signUpState.value = AuthFormState()
                onSuccess()
            }.onFailure {
                _signUpState.value = _signUpState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Sign up failed"
                )
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken).onSuccess {
                onSuccess()
            }.onFailure {
                _loginState.value = _loginState.value.copy(errorMessage = it.message)
            }
        }
    }

    fun completeOnboarding(
        uid: String,
        email: String,
        role: UserRole,
        form: OnboardingFormState,
        onSuccess: () -> Unit
    ) {
        _onboardingState.value = form.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            // Source email primarily from Auth, secondary from form, tertiary from param
            val firebaseEmail = FirebaseAuth.getInstance().currentUser?.email
            val finalEmail = firebaseEmail?.takeIf { it.isNotBlank() } 
                ?: email.takeIf { it.isNotBlank() } 
                ?: ""

            val user = LifeLineUser(
                uid = uid,
                email = finalEmail,
                displayName = form.displayName,
                role = role,
                bloodGroup = form.bloodGroup,
                city = form.city,
                hospitalName = form.hospitalName
            )
            
            if (user.email.isBlank()) {
                android.util.Log.e("AuthViewModel", "Critical: Saving user with blank email!")
            }

            authRepository.completeOnboarding(user).onSuccess {
                _onboardingState.value = OnboardingFormState()
                onSuccess()
            }.onFailure {
                _onboardingState.value = _onboardingState.value.copy(
                    isLoading = false,
                    errorMessage = it.message
                )
            }
        }
    }

    fun signOut() = authRepository.signOut()
}
