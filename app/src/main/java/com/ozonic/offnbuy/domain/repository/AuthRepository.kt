package com.ozonic.offnbuy.domain.repository

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.ozonic.offnbuy.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getAuthStateStream(): Flow<User?>
    suspend fun signInAnonymouslyIfNeeded()
    fun startPhoneNumberVerification(
        phone: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )
    // --- THIS IS THE CHANGE ---
    // Now returns the new User object and the old anonymous UID
    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Pair<User, String?>
    suspend fun signOut()
    suspend fun refreshUser()
    suspend fun updateDisplayName(displayName: String)
    suspend fun updateProfilePicture(uri: Uri)
    suspend fun verifyBeforeUpdateEmail(email: String)
    suspend fun updatePhoneNumber(credential: PhoneAuthCredential)
}