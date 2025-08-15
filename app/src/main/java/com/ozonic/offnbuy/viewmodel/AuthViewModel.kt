package com.ozonic.offnbuy.viewmodel

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.model.User
import com.ozonic.offnbuy.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State machine to manage the complex auth flow
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class AwaitingOtp(val verificationId: String, val phoneNumber: String) : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class AuthError(val message: String) : AuthState()
    object Loading : AuthState()
}

// We add a new, dedicated state for the phone linking dialog
sealed class UpdatePhoneState {
    object Idle : UpdatePhoneState()
    object Loading : UpdatePhoneState()
    data class AwaitingOtp(val verificationId: String) : UpdatePhoneState()
    data class Error(val message: String) : UpdatePhoneState()
}

sealed class EmailVerifyState {
    object Idle : EmailVerifyState()
    object Loading : EmailVerifyState()
    object OtpSent : EmailVerifyState() // State to trigger the dialog
    object Verified : EmailVerifyState() // State for success
    data class Error(val message: String) : EmailVerifyState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    private val _updatePhoneState = MutableStateFlow<UpdatePhoneState>(UpdatePhoneState.Idle)
    val updatePhoneState: StateFlow<UpdatePhoneState> = _updatePhoneState

    private val _emailVerifyState = MutableStateFlow<EmailVerifyState>(EmailVerifyState.Idle)
    val emailVerifyState: StateFlow<EmailVerifyState> = _emailVerifyState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    var previousAuthState: AuthState = AuthState.Unauthenticated
        private set

    init {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
        }
    }

    /**
     * Simulates sending an OTP to the user's email.
     * In a real app, this would call a backend service.
     */
    fun sendEmailOtp(email: String) {
        viewModelScope.launch {
            _emailVerifyState.value = EmailVerifyState.Loading
            // --- SIMULATION ---
            // This is where you would call your server to send a real OTP.
            // For now, we just move to the next state to show the dialog.
            println("SIMULATING OTP: Tell the user to enter 'password123' as the OTP for $email")
            _emailVerifyState.value = EmailVerifyState.OtpSent
            // --- END SIMULATION ---
        }
    }

    /**
     * Takes the email and user-entered OTP to link the credential to the account.
     */
    fun verifyEmailAndLink(email: String, otp: String) {
        viewModelScope.launch {
            _emailVerifyState.value = EmailVerifyState.Loading
            try {
                repository.linkEmailCredential(email, otp)
                val updatedUser = repository.reloadUser() // Refresh user data
                _authState.value = AuthState.Authenticated(updatedUser!!)
                _emailVerifyState.value = EmailVerifyState.Verified
            } catch (e: Exception) {
                _emailVerifyState.value = EmailVerifyState.Error(e.message ?: "Verification failed. The email might be in use or the OTP is incorrect.")
            }
        }
    }

    fun resetEmailVerifyState() {
        _emailVerifyState.value = EmailVerifyState.Idle
    }

    /**
     * Reloads the current user's state from Firebase to get updates
     * like email verification status.
     */
    fun reloadUser() {
        viewModelScope.launch {
            try {
                val updatedUser = repository.reloadUser()
                if (updatedUser != null) {
                    _authState.value = AuthState.Authenticated(updatedUser)
                }
            } catch (e: Exception) {
                // Silently fail or log error, no need to show UI error for a background refresh
            }
        }
    }

    /**
     * Sends a verification link to the provided email address.
     */
    fun sendVerificationEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.updateAndVerifyEmail(email)
                val updatedUser = repository.getCurrentUser()!!
                _authState.value = AuthState.Authenticated(updatedUser)
            } catch (e: Exception) {
                _authState.value = AuthState.AuthError(e.message ?: "Failed to send verification email.")
                Log.d("VerifyEmail", "Error sending verification email ${e.message}")
            }
        }
    }

    /**
     * Updates just the user's display name.
     */
    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.updateDisplayName(displayName)
                val updatedUser = repository.getCurrentUser()!!
                _authState.value = AuthState.Authenticated(updatedUser)
            } catch (e: Exception) {
                _authState.value = AuthState.AuthError(e.message ?: "Profile update failed")
            }
        }
    }

    /**
     * Initiates the phone number UPDATE flow by sending an OTP to the new number.
     */
    fun sendOtpForUpdate(phone: String, activity: Activity) {
        _updatePhoneState.value = UpdatePhoneState.Loading
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval case
                updateUserPhoneNumber(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _updatePhoneState.value = UpdatePhoneState.Error(e.message ?: "Verification failed.")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _updatePhoneState.value = UpdatePhoneState.AwaitingOtp(verificationId)
            }
        }
        repository.sendOtpToPhone(phone, activity, callbacks)
    }

    /**
     * Finalizes the update using the credential from the verified OTP.
     */
    fun updateUserPhoneNumber(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _updatePhoneState.value = UpdatePhoneState.Loading
            try {
                repository.updatePhoneNumber(credential)
                val updatedUser = repository.getCurrentUser()!!
                _authState.value = AuthState.Authenticated(updatedUser) // Refresh the main auth state
                _updatePhoneState.value = UpdatePhoneState.Idle // Reset dialog state on success
            } catch (e: Exception) {
                _updatePhoneState.value = UpdatePhoneState.Error(e.message ?: "Failed to update phone number.")
            }
        }
    }

    /**
     * Resets the update phone number dialog state, typically on dismiss.
     */
    fun resetLinkState() {
        _updatePhoneState.value = UpdatePhoneState.Idle
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.updateProfilePicture(uri)
                _authState.value = AuthState.Authenticated(repository.getCurrentUser()!!)
            } catch (e: Exception) {
                _authState.value = AuthState.AuthError(e.message ?: "Image upload failed.")
            }
        }
    }

    // --- Sign-in/Sign-up Flows (Simplified) ---
    private fun handleSignInResult(result: AuthResult) {
        viewModelScope.launch {
            _user.value = mapFirebaseUser(result.user)
            _authState.value = AuthState.Authenticated(result.user!!)
            val user = result.user!!
            if (result.additionalUserInfo?.isNewUser == true) {
                // For new users, create their profile document and then authenticate
                repository.createUsername(user)
            }
            _authState.value = AuthState.Authenticated(user)
        }
    }

    private fun mapFirebaseUser(firebaseUser: FirebaseUser?): User? {
        return firebaseUser?.let {
            User(
                uid = it.uid,
                displayName = it.displayName,
                email = it.email,
                phoneNumber = it.phoneNumber,
                photoUrl = it.photoUrl?.toString(),
                isEmailVerified = it.isEmailVerified
            )
        }
    }
    fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.signInWithPhoneCredential(credential)
                handleSignInResult(result)
            } catch (e: Exception) {
                _authState.value = AuthState.AuthError(e.message ?: "Sign-in failed")
            }
        }
    }

    fun sendOtp(phone: String, activity: Activity) {
        // --- ADD THIS LINE ---
        // Before we start loading, we record the current state.
        previousAuthState = _authState.value

        _authState.value = AuthState.Loading
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhone(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.AuthError(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _authState.value = AuthState.AwaitingOtp(verificationId, phone)
            }
        }
        repository.sendOtpToPhone(phone, activity, callbacks)
    }

    fun verifyOtp(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhone(credential)
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Unauthenticated
    }

    fun goBackToLogin() {
        _authState.value = AuthState.Unauthenticated
    }
}