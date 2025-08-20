package com.ozonic.offnbuy.viewmodel

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.data.AppDatabase
import com.ozonic.offnbuy.model.User
import com.ozonic.offnbuy.repository.AuthRepository
import com.ozonic.offnbuy.repository.ReAuthenticationRequiredException
import com.ozonic.offnbuy.repository.UserDataRepository
import com.ozonic.offnbuy.ui.screens.LoadingOverlay
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

    private val repository = AuthRepository()
    private val userDataRepository: UserDataRepository
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        val db = AppDatabase.getDatabase(application)
        userDataRepository = UserDataRepository(db.favoriteDealDao(), db.generatedLinkDao())

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
                // Step 1: Get the latest user authentication status from Firebase.
                val freshUser = repository.reloadUser()

                if (freshUser != null) {
                    val currentAuthState = _authState.value

                    // Step 2: Immediately update the UI with the fresh user profile.
                    // This makes the app feel instantly responsive.
                    _authState.value = AuthState.Authenticated(freshUser)

                    // Step 3: Check if a full data sync is needed (e.g., on first load or user change).
                    if (currentAuthState !is AuthState.Authenticated || currentAuthState.user.uid != freshUser.uid) {
                        // Launch a separate coroutine for the heavy sync operations.
                        // This allows them to run in the background without blocking the UI.
                        viewModelScope.launch {
                            userDataRepository.syncFavorites()
                            userDataRepository.syncGeneratedLinks()
                        }
                    }

                    // Handle email verification update (this is a quick operation).
                    if (freshUser.isEmailVerified && freshUser.email != null) {
                        if (currentAuthState is AuthState.Authenticated && !currentAuthState.user.isEmailVerified) {
                            repository.updateUserEmailInDb(freshUser.uid, freshUser.email)
                        }
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: ReAuthenticationRequiredException) {
                _authState.value = AuthState.ReAuthenticationRequired(e.message)
            }
        }
    }
    // In offnbuy/viewmodel/AuthViewModel.kt

    // In offnbuy/viewmodel/AuthViewModel.kt

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        // The isVerifying flag is set by the caller (verifyOtp), so the UI
        // correctly shows a loading state on the OTP screen.

        viewModelScope.launch {
            try {
                // --- Step 1: Perform the authentication ONLY ---
                Log.d("AuthFlow", "Starting signInOrLinkUser...")
                val (newUser, discardedAnonymousUid) = repository.signInOrLinkUser(credential)
                Log.d("AuthFlow", "signInOrLinkUser successful. NewUser: ${newUser?.uid}, Discarded UID: $discardedAnonymousUid")

                if (newUser == null) {
                    throw IllegalStateException("Authentication resulted in a null user.")
                }

                // --- Step 2: IMMEDIATELY update the state to Authenticated ---
                // This is the most critical change. It tells the UI that the login was successful,
                // which will trigger the navigation away from the AuthScreen.
                _authState.value = AuthState.Authenticated(newUser)
                Log.d("AuthFlow", "State set to Authenticated. Login process finished successfully.")

                // --- Step 3: Handle data sync as a SEPARATE background task ---
                if (discardedAnonymousUid != null) {
                    // This was a returning user, so we need to clean up the old guest data and sync the real data.
                    // We launch a new coroutine so it doesn't block or fail the main login flow.
                    viewModelScope.launch {
                        try {
                            Log.d("AuthFlow", "Returning user detected. Clearing local data for $discardedAnonymousUid and syncing...")
                            userDataRepository.clearAllLocalDataForUser(discardedAnonymousUid)
                            userDataRepository.syncFavorites()
                            userDataRepository.syncGeneratedLinks()
                            Log.d("AuthFlow", "Background data sync complete.")
                        } catch (syncError: Exception) {
                            // Log the sync error, but DO NOT change the auth state.
                            // The user is successfully logged in, even if the sync failed.
                            Log.e("AuthFlow", "Background data sync failed after login: ", syncError)
                        }
                    }
                }

            } catch (e: Exception) {
                // This block now only catches REAL authentication failures (e.g., invalid OTP).
                Log.e("AuthFlow", "CRITICAL ERROR in signInWithPhone catch block: ", e)

                val errorMessage = if (e is FirebaseAuthInvalidCredentialsException) {
                    "The code you entered is incorrect. Please try again."
                } else {
                    "Login failed. Please try again."
                }

                // Return the user to the OTP screen with a specific error.
                val currentState = _authState.value
                if (currentState is AuthState.AwaitingOtp) {
                    _authState.value = currentState.copy(isVerifying = false, error = errorMessage)
                } else {
                    // As a fallback, reset to a clean unauthenticated state.
                    _authState.value = AuthState.AuthError(errorMessage)
                    repository.signInAnonymouslyIfNeeded()
                }
            }
        }
    }

    fun logout(){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            userDataRepository.clearAllLocalDataForUser()
            repository.logout()
            val anonymousUser = repository.signInAnonymouslyIfNeeded()
            if (anonymousUser != null) {
                _authState.value = AuthState.Authenticated(anonymousUser)
            } else {
                _authState.value = AuthState.Unauthenticated
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
        val currentState = _authState.value
        if (currentState is AuthState.AwaitingOtp) {
            _authState.value = currentState.copy(isVerifying = true, error = null)
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhone(credential)
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
}