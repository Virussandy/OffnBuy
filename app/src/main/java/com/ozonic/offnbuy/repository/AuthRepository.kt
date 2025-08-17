package com.ozonic.offnbuy.repository

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ozonic.offnbuy.model.User
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit


class ReAuthenticationRequiredException(message: String) : Exception(message)
class AuthRepository {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun mapFirebaseUser(firebaseUser: FirebaseUser?): User? {
        return firebaseUser?.let {
            User(
                uid = it.uid,
                displayName = it.displayName,
                email = it.email,
                phoneNumber = it.phoneNumber,
                photoUrl = it.photoUrl,
                isEmailVerified = it.isEmailVerified
            )
        }
    }

    suspend fun reloadUser(): User? {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("AuthFlow", "AuthRepository: reloadUser() called, but currentUser is already null.")
            return null
        }

        return try {
            Log.d("AuthFlow", "AuthRepository: Attempting to reload user data for UID: ${currentUser.uid}")
            currentUser.reload().await()
            Log.d("AuthFlow", "AuthRepository: User data reloaded successfully.")
            mapFirebaseUser(auth.currentUser)
        } catch (e: FirebaseAuthInvalidUserException) {
            // This is the key error. Throw our custom exception.
            auth.signOut()
            throw ReAuthenticationRequiredException("Your email has been successfully verified. Please sign in again to continue")
        } catch (e: Exception) {
            auth.signOut()
            null
        }
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): AuthResult {
        return auth.signInWithCredential(credential).await()
    }

    fun sendOtpToPhone(
        phone: String,
        activity: Activity,
        resendToken: PhoneAuthProvider.ForceResendingToken?,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        resendToken?.let {
            optionsBuilder.setForceResendingToken(it)
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    suspend fun createUserProfile(user: FirebaseUser?) {
        if (user == null) return
        db.collection("users").document(user.uid).set(
            mapOf(
                "name" to (user.displayName ?: "User"),
                "profilePic" to user.photoUrl?.toString(),
                "email" to user.email,
                "phone" to user.phoneNumber,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun updateDisplayName(displayName: String) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
        user.updateProfile(profileUpdates).await()
        db.collection("users").document(user.uid).update("name", displayName).await()
    }

    suspend fun verifyBeforeUpdateEmail(email: String) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")
        try {
            user.verifyBeforeUpdateEmail(email).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            throw Exception("This email is already in use by another account.")
        } catch (e: Exception) {
            throw Exception("Failed to send verification email. Please try again.")
        }
    }

    suspend fun updateUserEmailInDb(uid: String, email: String) {
        db.collection("users").document(uid).update("email", email).await()
    }

    suspend fun updateProfilePicture(uri: Uri) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")
        val storageRef = storage.reference.child("profile_pictures/${user.uid}")
        val downloadUrl = storageRef.putFile(uri).await().storage.downloadUrl.await()
        val profileUpdates = UserProfileChangeRequest.Builder().setPhotoUri(downloadUrl).build()
        user.updateProfile(profileUpdates).await()
        db.collection("users").document(user.uid).update("profilePic", downloadUrl.toString()).await()
    }

    suspend fun updatePhoneNumber(credential: PhoneAuthCredential) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")
        try {
            user.updatePhoneNumber(credential).await()
            val updatedPhoneNumber = auth.currentUser?.phoneNumber
            if (updatedPhoneNumber != null) {
                db.collection("users").document(user.uid).update("phone", updatedPhoneNumber).await()
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            throw Exception("This phone number is already linked to another account.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw Exception("The verification code is incorrect. Please try again.")
        } catch (e: Exception) {
            throw Exception("An unexpected error occurred. Please try again later.")
        }
    }

    fun logout() {
        auth.signOut()
    }
}