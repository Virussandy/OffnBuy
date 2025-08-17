package com.ozonic.offnbuy.viewmodel

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.model.User
import com.ozonic.offnbuy.repository.AuthRepository
import com.ozonic.offnbuy.repository.ReAuthenticationRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class AwaitingOtp(
        val verificationId: String,
        val phoneNumber: String,
        val resendToken: PhoneAuthProvider.ForceResendingToken?,
        val error: String? = null,
        val isResending: Boolean = false
    ) : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class AuthError(val message: String) : AuthState()
    data class ReAuthenticationRequired(val message: String?) : AuthState()
    object Loading : AuthState()
}

data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdateSuccessful: Boolean = false,
    val phoneVerificationId: String? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        // Set initial state from cached user. The global refresh will handle fetching fresh data.
        val cachedUser = repository.mapFirebaseUser(repository.auth.currentUser)
        _authState.value = if (cachedUser != null) AuthState.Authenticated(cachedUser) else AuthState.Unauthenticated
    }

    fun refreshUser() {
        if (_authState.value is AuthState.Loading) return
        viewModelScope.launch {
            try {
                val freshUser = repository.reloadUser()
                if (freshUser != null) {
                    val currentAuthState = _authState.value
                    if (freshUser.isEmailVerified && freshUser.email != null) {
                        if (currentAuthState is AuthState.Authenticated && !currentAuthState.user.isEmailVerified) {
                            repository.updateUserEmailInDb(freshUser.uid, freshUser.email)
                        }
                    }
                    _authState.value = AuthState.Authenticated(freshUser)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: ReAuthenticationRequiredException) {
                // --- THIS IS THE FIX ---
                _authState.value = AuthState.ReAuthenticationRequired(e.message)
            }
        }
    }

    fun sendOtp(phone: String, activity: Activity, resendToken: PhoneAuthProvider.ForceResendingToken? = null) {
        val currentState = _authState.value

        // If an OTP is already pending for the SAME number, do nothing.
        // The UI will see the 'AwaitingOtp' state and stay on the correct screen.
        if (currentState is AuthState.AwaitingOtp && currentState.phoneNumber == phone && resendToken == null) {
            Log.d("AuthFlow", "AuthViewModel: OTP already pending for $phone. Not sending another.")
            return
        }

        if (resendToken != null && currentState is AuthState.AwaitingOtp) {
            _authState.value = currentState.copy(isResending = true, error = null)
        } else {
            _authState.value = AuthState.Loading
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) { signInWithPhone(credential) }
            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.AuthError(e.message ?: "Verification Failed")
            }
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                _authState.value = AuthState.AwaitingOtp(id, phone, token)
            }
            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                // The 'onCodeSent' callback might have already been called,
                // so we only update the state if we are still in a loading state.
                if (_authState.value is AuthState.Loading) {
                    val currentState = _authState.value
                    if (currentState is AuthState.AwaitingOtp) {
                        _authState.value = currentState.copy(error = "SMS auto-retrieval timed out.")
                    }
                }
            }
        }
        repository.sendOtpToPhone(phone, activity, resendToken, callbacks)
    }

    fun verifyOtp(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhone(credential)
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val authResult = repository.signInWithPhoneCredential(credential)
                if (authResult.additionalUserInfo?.isNewUser == true) {
                    repository.createUserProfile(authResult.user)
                }
                val user = repository.mapFirebaseUser(authResult.user)
                    ?: throw IllegalStateException("User null after sign in")
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                val currentOtpState = _authState.value
                val errorMessage = if (e is FirebaseAuthInvalidCredentialsException) {
                    "The code you entered is incorrect. Please try again."
                } else {
                    e.message ?: "Sign-in failed"
                }

                if (currentOtpState is AuthState.AwaitingOtp) {
                    _authState.value = currentOtpState.copy(error = errorMessage)
                } else {
                    _authState.value = AuthState.AuthError(errorMessage)
                }
            }
        }
    }

    fun cancelVerification() {
        _authState.value = AuthState.Unauthenticated
    }

    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                repository.updateDisplayName(displayName)
                refreshUser()
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.message)
            }
        }
    }

    fun sendVerificationEmail(email: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                repository.verifyBeforeUpdateEmail(email)
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.message)
            }
        }
    }

    fun resetProfileState() {
        _profileState.value = ProfileState()
    }

    fun sendOtpForUpdate(phone: String, activity: Activity) {
        _profileState.value = ProfileState(isLoading = true)
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                updateUserPhoneNumber(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                _profileState.value = ProfileState(error = e.message)
            }
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _profileState.value = ProfileState(phoneVerificationId = verificationId)
            }
        }
        repository.sendOtpToPhone(phone, activity, null, callbacks)
    }

    fun updateUserPhoneNumber(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                repository.updatePhoneNumber(credential)
                refreshUser()
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.message)
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                repository.updateProfilePicture(uri)
                refreshUser()
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.message)
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Unauthenticated
    }
}