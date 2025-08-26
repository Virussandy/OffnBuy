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

// A sealed interface representing the distinct states of the authentication UI.
sealed interface AuthUiState {
    object Idle : AuthUiState // Initial state
    object Loading : AuthUiState // For showing a global loading indicator
    data class EnterPhoneNumber(val error: String? = null) : AuthUiState
    data class EnterOtp(val phoneNumber: String, val error: String? = null) : AuthUiState
    object AuthSuccess : AuthUiState
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

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Holds the latest, valid verificationId from Firebase.
    private var phoneVerificationId: String? = null

    // --- User Session and Profile Flows ---
    val currentUser: StateFlow<User?> = authRepository.getAuthStateStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfile?> = currentUser.flatMapLatest { user ->
        user?.uid?.let { userDataRepository.getUserProfile(it) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Unchanged profile state
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        // Ensure we always have a user session, even an anonymous one.
        viewModelScope.launch {
            authRepository.signInAnonymouslyIfNeeded()
            // Start the UI in the phone number entry state.
            _uiState.value = AuthUiState.EnterPhoneNumber()
        }
    }

    /**
     * Kicks off the phone number verification process.
     */
    fun onPhoneNumberEntered(phoneNumber: String, activity: Activity) {
        _uiState.value = AuthUiState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // This is the callback for auto-verification.
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.value = AuthUiState.EnterPhoneNumber(error = e.localizedMessage ?: "Verification failed.")
            }

            // This is the callback after the SMS has been sent.
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                phoneVerificationId = verificationId
                _uiState.value = AuthUiState.EnterOtp(phoneNumber)
            }
        }
        authRepository.startPhoneNumberVerification(phoneNumber, activity, callbacks)
    }

    /**
     * Verifies the OTP code entered by the user.
     */
    fun onOtpEntered(otp: String) {
        // Use the stored verificationId to create the credential.
        phoneVerificationId?.let { verId ->
            _uiState.value = AuthUiState.Loading
            val credential = PhoneAuthProvider.getCredential(verId, otp)
            signInWithCredential(credential)
        } ?: run {
            // This case handles if the user tries to verify without a valid verification ID.
            _uiState.value = AuthUiState.EnterPhoneNumber(error = "Verification process expired. Please try again.")
        }
    }

    /**
     * Central function to handle the final sign-in and data migration.
     */
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val (newUser, discardedAnonUid) = authRepository.signInWithPhoneAuthCredential(credential)

                // NEW: Create the user profile document right after successful sign-in.
                createUserProfileIfNeeded(newUser)

                if (discardedAnonUid != null) {
                    userDataRepository.migrateLocalUserData(discardedAnonUid, newUser.uid)
                }
                _uiState.value = AuthUiState.AuthSuccess
            } catch (e: Exception) {
                val phoneNumber = (_uiState.value as? AuthUiState.EnterOtp)?.phoneNumber ?: ""
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "The code you entered is incorrect. Please try again."
                    else -> e.localizedMessage ?: "An unknown authentication error occurred."
                }
                _uiState.value = AuthUiState.EnterOtp(phoneNumber, error = errorMessage)
            }
        }
    }

    // --- Your existing profile management functions remain unchanged ---
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState.EnterPhoneNumber()
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                authRepository.updateDisplayName(name)
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.message)
            }
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

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                authRepository.updateProfilePicture(uri)
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.message)
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
                _profileState.value = ProfileState(error = e.message)
            }
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

    fun updatePhoneNumber(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            try {
                authRepository.updatePhoneNumber(credential)
                _profileState.value = ProfileState(isUpdateSuccessful = true)
            } catch (e: Exception) {
                _profileState.value = ProfileState(error = e.message)
            }
        }
    }

    fun resetProfileState() {
        _profileState.value = ProfileState()
    }
}