package com.ozonic.offnbuy.viewmodel

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.data.AppDatabase
import com.ozonic.offnbuy.model.User
import com.ozonic.offnbuy.repository.AuthRepository
import com.ozonic.offnbuy.repository.ReAuthenticationRequiredException
import com.ozonic.offnbuy.repository.UserDataRepository
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
import kotlinx.coroutines.delay
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
        val isResending: Boolean = false,
        val isVerifying: Boolean = false
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

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AuthRepository(NetworkConnectivityObserver(application)) // Made public for UserDataManager
    private val userDataRepository: UserDataRepository
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    init {

        val db = AppDatabase.getDatabase(application) // Add this
        userDataRepository = UserDataRepository(db.favoriteDealDao(), db.generatedLinkDao(), db.userProfileDao()) // Add this
        // The ViewModel's only job on init is to establish the initial user session.
        // The UserDataManager will listen to this and handle all data syncing.
        viewModelScope.launch {
            val user = repository.signInAnonymouslyIfNeeded()
            _authState.value = if (user != null) {
                AuthState.Authenticated(user)
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    fun refreshUser() {
        if (_authState.value is AuthState.Loading) return
        viewModelScope.launch {
            try {
                val freshUser = repository.reloadUser()
                _authState.value = if (freshUser != null) {
                    AuthState.Authenticated(freshUser)
                } else {
                    AuthState.Unauthenticated
                }
            } catch (e: ReAuthenticationRequiredException) {
                _authState.value = AuthState.ReAuthenticationRequired(e.message)
            }
        }
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val (newUser, discardedAnonymousUid) = repository.signInOrLinkUser(credential)
                if (newUser == null) throw IllegalStateException("User object was null after authentication.")

                // --- THIS IS THE KEY MIGRATION LOGIC ---
                if (discardedAnonymousUid != null) {
                    // An anonymous user has just signed in. Merge their local data.
                    userDataRepository.migrateLocalUserData(
                        fromUid = discardedAnonymousUid,
                        toUid = newUser.uid
                    )
                }
                // --- END OF MIGRATION LOGIC ---

                _authState.value = AuthState.Authenticated(newUser)

            } catch (e: Exception) {
                val errorMessage = if (e is FirebaseAuthInvalidCredentialsException) {
                    "The code you entered is incorrect. Please try again."
                } else {
                    "Login failed. Please try again."
                }

                viewModelScope.launch {
                    delay(1000)
                    val currentState = _authState.value
                    if (currentState is AuthState.AwaitingOtp) {
                        _authState.value = currentState.copy(isVerifying = false, error = errorMessage)
                    } else {
                        _authState.value = AuthState.AuthError(errorMessage)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // The UserDataManager will automatically detect this user change and clear all local data.
            repository.logout()
            val anonymousUser = repository.signInAnonymouslyIfNeeded()
            _authState.value = if (anonymousUser != null) {
                AuthState.Authenticated(anonymousUser)
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    fun sendOtp(
        phone: String,
        activity: Activity,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ) {
        val currentState = _authState.value
        if (currentState is AuthState.AwaitingOtp && currentState.phoneNumber == phone && resendToken == null) {
            return
        }

        if (resendToken != null && currentState is AuthState.AwaitingOtp) {
            _authState.value = currentState.copy(isResending = true, error = null)
        } else {
            _authState.value = AuthState.Loading
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // 🔒 Skip if already authenticated OR manual verification started
                val state = _authState.value
                if (state is AuthState.Authenticated || (state is AuthState.AwaitingOtp && state.isVerifying)) {
                    return
                }
                signInWithPhone(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.AuthError(e.message ?: "Verification Failed")
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                _authState.value = AuthState.AwaitingOtp(id, phone, token)
            }
        }

        repository.sendOtpToPhone(phone, activity, resendToken, callbacks)
    }

    fun verifyOtp(verificationId: String, code: String) {
        val currentState = _authState.value
        if (currentState is AuthState.AwaitingOtp) {
            // 🔒 Skip if already authenticated or currently verifying
            if (_authState.value is AuthState.Authenticated || currentState.isVerifying) return

            _authState.value = currentState.copy(isVerifying = true, error = null)
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhone(credential)
        }
    }

    fun cancelVerification() {
        // When canceling, we must ensure we are in a valid anonymous state.
        viewModelScope.launch {
            val user = repository.signInAnonymouslyIfNeeded()
            _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
        }
    }

    // All profile update functions remain the same as they are direct user actions.
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
                repository.updateUserEmailInDb(email)
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
                // Prevent double-call if already authenticated
                if (_authState.value is AuthState.Authenticated) return
                signInWithPhone(credential)
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
}