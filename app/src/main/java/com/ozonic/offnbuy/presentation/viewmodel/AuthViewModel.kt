package com.ozonic.offnbuy.presentation.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.domain.model.User
import com.ozonic.offnbuy.domain.model.UserProfile
import com.ozonic.offnbuy.domain.repository.AuthRepository
import com.ozonic.offnbuy.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * A sealed interface representing the different authentication states.
 * This class is a key part of the UI's reactive state machine.
 */
sealed interface AuthState {
    /** The initial state, used before any user status is determined. */
    object Initializing : AuthState

    /** The state when no user is signed in, or the user is anonymous. */
    object Unauthenticated : AuthState

    /** The state when a user is signed in. */
    data class Authenticated(val user: User) : AuthState

    /** The state when an OTP has been sent and is awaiting verification. */
    data class AwaitingOtp(
        val verificationId: String,
        val phoneNumber: String,
        val resendToken: PhoneAuthProvider.ForceResendingToken?,
        val error: String? = null,
        val isVerifying: Boolean = false,
        val isResending: Boolean = false,
        val autoVerifiedOtp: String? = null // This property will hold the auto-retrieved code
    ) : AuthState

    /** The state when an authentication error has occurred. */
    data class AuthError(val message: String) : AuthState

    /** The state when a long-running operation is in progress. */
    object Loading : AuthState
    object AuthSuccess : AuthState
}

// Separate state for profile updates (no changes needed here)
data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdateSuccessful: Boolean = false,
    val phoneVerificationId: String? = null
)


class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var phoneVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var lastPhoneNumber: String? = null

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.getAuthStateStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfile?> = currentUser.flatMapLatest { user ->
        user?.uid?.let { userDataRepository.getUserProfile(it) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    init {
        // Observe the user and update the AuthState accordingly
        viewModelScope.launch {
            authRepository.getAuthStateStream().collect { user ->
                _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
            }
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                authRepository.updateDisplayName(name)
                // The listener will update the userProfile flow automatically.
                // We can set success immediately.
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.localizedMessage)
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                authRepository.updateProfilePicture(uri)
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.localizedMessage)
            }
        }
    }

    fun verifyBeforeUpdateEmail(email: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                authRepository.verifyBeforeUpdateEmail(email)
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.localizedMessage)
            }
        }
    }

    fun updatePhoneNumber(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                authRepository.updatePhoneNumber(credential)
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.localizedMessage)
            }
        }
    }


    fun sendOtp(phoneNumber: String, activity: Activity, token: PhoneAuthProvider.ForceResendingToken? = null) {
        _authState.value = AuthState.Loading
        lastPhoneNumber = phoneNumber

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val otp = credential.smsCode
                val currentState = _authState.value

                // Check if we are in the AwaitingOtp state and have a code
                if (otp != null && currentState is AuthState.AwaitingOtp) {
                    // Update the existing state with the auto-retrieved code
                    _authState.value = currentState.copy(autoVerifiedOtp = otp)
                } else {
                    // Fallback to direct sign-in if we aren't on the OTP screen
                    signInWithCredential(credential)
                }
            }
            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.AuthError(e.localizedMessage ?: "Verification failed.")
            }

            override fun onCodeSent(verificationId: String, newToken: PhoneAuthProvider.ForceResendingToken) {
                phoneVerificationId = verificationId
                resendToken = newToken
                _authState.value = AuthState.AwaitingOtp(
                    verificationId = verificationId,
                    phoneNumber = phoneNumber,
                    resendToken = newToken
                )
            }
        }

        authRepository.startPhoneNumberVerification(phoneNumber, activity, callbacks)
    }

    fun verifyOtp(verificationId: String, otp: String) {
        _authState.value = AuthState.AwaitingOtp(
            verificationId = verificationId,
            phoneNumber = lastPhoneNumber ?: "",
            resendToken = resendToken,
            isVerifying = true
        )

        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    fun cancelVerification() {
        phoneVerificationId = null
        resendToken = null
        lastPhoneNumber = null
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Central function to handle the final sign-in and data migration.
     */
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                userDataRepository.clearAllLocalUserData()
                val newUser = authRepository.signInWithPhoneAuthCredential(credential)

                // ADD THIS LINE: Proactively fetch the profile
                userDataRepository.fetchAndCacheUserProfile(newUser.uid)

                createUserProfileIfNeeded(newUser)

                _authState.value = AuthState.AuthSuccess
            } catch (e: Exception) {
                _authState.value = AuthState.AuthError(e.localizedMessage ?: "Authentication failed.")
            }
        }
    }

    // --- Your existing profile management functions remain unchanged ---
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userDataRepository.clearAllLocalUserData()
        }
    }

    /**
     * Creates the user profile document in Firestore if it doesn't already exist.
     * This is called AFTER a successful sign-in to avoid race conditions.
     */
    private suspend fun createUserProfileIfNeeded(user: User) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
        if (!userDocRef.get().await().exists()) {
            val profileData = mapOf(
                "uid" to user.uid,
                "name" to (user.displayName ?: "User"),
                "profilePic" to user.photoUrl?.toString(),
                "email" to user.email,
                "phone" to user.phoneNumber,
                "createdAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(profileData).await()
        }
    }

    fun sendOtpForUpdate(phone: String, activity: Activity) {
        _profileState.value = ProfileState(isLoading = true)
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                updatePhoneNumber(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _profileState.value = ProfileState(error = e.message)
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                _profileState.value = ProfileState(phoneVerificationId = id)
            }
        }
        authRepository.startPhoneNumberVerification(phone, activity, callbacks)
    }

    fun resetProfileState() {
        _profileState.value = ProfileState()
    }
}