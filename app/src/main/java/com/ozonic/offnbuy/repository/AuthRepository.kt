package com.ozonic.offnbuy.repository

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun reloadUser(): FirebaseUser? {
        auth.currentUser?.reload()?.await()
        return auth.currentUser
    }

    // --- Phone & OTP Auth ---
    fun sendOtpToPhone(
        phone: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for reCAPTCHA)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Updates only the display name in Auth and Firestore.
     */
    suspend fun updateDisplayName(displayName: String) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()

        db.collection("users").document(user.uid).update("name", displayName).await()
    }

    /**
     * Updates the email in Auth, sends the verification link, and updates Firestore.
     */
    suspend fun updateAndVerifyEmail(email: String) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")

        user.updateEmail(email).await()
        user.sendEmailVerification().await()
        db.collection("users").document(user.uid).update("email", email).await()
    }

    /**
     * Uploads a new profile picture and updates the photoUrl in Auth and Firestore.
     */
    suspend fun updateProfilePicture(uri: Uri) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")
        val storageRef = storage.reference.child("profile_pictures/${user.uid}")

        val downloadUrl = storageRef.putFile(uri).await()
            .storage.downloadUrl.await()

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(downloadUrl)
            .build()
        user.updateProfile(profileUpdates).await()

        db.collection("users").document(user.uid).update("profilePic", downloadUrl.toString()).await()
    }

    /**
     * Updates the user's primary phone number in Firebase Auth after verification
     * and syncs the change to Firestore.
     */
    suspend fun updatePhoneNumber(credential: PhoneAuthCredential) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")
        user.updatePhoneNumber(credential).await()

        // After the update is successful, sync the new number to Firestore
        val updatedPhoneNumber = auth.currentUser?.phoneNumber
        if (updatedPhoneNumber != null) {
            db.collection("users").document(user.uid).update("phone", updatedPhoneNumber).await()
        }
    }

    /**
     * Creates an EmailAuthCredential and links it to the currently signed-in user.
     * This is the correct way to ADD a new email to an existing account.
     * NOTE: For this to work, the "OTP" is treated as a temporary password.
     */
    suspend fun linkEmailCredential(email: String, otp: String) {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in.")

        // Create a credential using the new email and the "OTP" as a temporary password.
        val credential = EmailAuthProvider.getCredential(email, otp)

        // Link this new credential to the existing user account.
        user.linkWithCredential(credential).await()

        // After successfully linking, update the email in Firestore.
        db.collection("users").document(user.uid).update("email", email).await()
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): AuthResult {
        return auth.signInWithCredential(credential).await()
    }

    suspend fun createUsername(user: FirebaseUser) {
        // Optionally, save user profile info in a 'users' collection
        db.collection("users").document(user.uid).set(mapOf(
            "name" to (user.displayName ?: "User"), // Use existing display name or default to "User"
            "profilePic" to user.photoUrl,
            "email" to user.email,
            "phone" to user.phoneNumber,
            "createdAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun linkCredential(credential: AuthCredential): AuthResult {
        val currentUser = auth.currentUser ?: throw IllegalStateException("No user is currently signed in.")
        return currentUser.linkWithCredential(credential).await()
    }

    suspend fun updateProfile(displayName: String?, email: String?) {
        val user = auth.currentUser ?: throw IllegalStateException("No user is currently signed in.")
        val oldEmail = user.email

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates).await()

        if (email != null && email != user.email && email != oldEmail) {
            user.updateEmail(email).await()
            user.sendEmailVerification().await()
        }

        // Now, update Firestore
        val userDocRef = db.collection("users").document(user.uid)
        val updates = mutableMapOf<String, Any?>()
        if (displayName != null) {
            updates["name"] = displayName
        }
        if (email != null) {
            updates["email"] = email
        }

        if (updates.isNotEmpty()) {
            userDocRef.update(updates).await()
        }
    }

    fun logout() {
        auth.signOut()
    }
}