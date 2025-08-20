package com.ozonic.offnbuy.repository

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ozonic.offnbuy.model.User
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

// Custom exception for clarity
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
                isEmailVerified = it.isEmailVerified,
                isAnonymous = it.isAnonymous
            )
        }
    }

    suspend fun reloadUser(): User? {
        val currentUser = auth.currentUser
        Log.d("OffnBuy",currentUser.toString())
        if (currentUser == null) {
            return signInAnonymouslyIfNeeded()
        }
        return try {
            currentUser.reload().await()
            mapFirebaseUser(auth.currentUser)
        } catch (e: FirebaseAuthInvalidUserException) {
            auth.signOut()
            throw ReAuthenticationRequiredException("Your session has expired. Please sign in again to continue.")
        } catch (e: Exception) {
            auth.signOut()
            return signInAnonymouslyIfNeeded()
        }
    }

    // Also, add a simple signOut function for clarity.
    fun signOut() {
        auth.signOut()
    }

    // In offnbuy/repository/AuthRepository.kt

    suspend fun signInOrLinkUser(credential: PhoneAuthCredential): Pair<User?, String?> {
        val anonymousUser = auth.currentUser
        val anonymousUid = anonymousUser?.uid

        try {
            // --- SCENARIO 1: Try to link the account (for new users) ---
            val linkResult = anonymousUser?.linkWithCredential(credential)?.await()
            val upgradedUser = mapFirebaseUser(linkResult?.user)

            // After linking, update the phone number in the Firestore profile
            if (upgradedUser?.phoneNumber != null) {
                db.collection("users").document(upgradedUser.uid).update("phone", upgradedUser.phoneNumber).await()
            }

            // Return the upgraded user and null for the anonymous ID (since it wasn't discarded)
            return Pair(upgradedUser, null)

        } catch (e: FirebaseAuthUserCollisionException) {
            // --- SCENARIO 2: Collision detected (this is a returning user) ---
            // 1. Sign out the temporary anonymous guest.
            signOut()

            // 2. Sign in the existing permanent user.
            val signInResult = auth.signInWithCredential(credential).await()
            val permanentUser = mapFirebaseUser(signInResult.user)

            // 3. Return the permanent user AND the old anonymous ID that needs to be cleaned up.
            return Pair(permanentUser, anonymousUid)
        }
    }

    /**
     * Silently signs in the user to an anonymous account if they are not logged in at all.
     * This is the key to ensuring there is always a userId to work with.
     */
    suspend fun signInAnonymouslyIfNeeded(): User? {
        if (auth.currentUser == null) {
            val authResult = auth.signInAnonymously().await()
            createUserProfile(authResult.user) // Create a basic profile for the new anonymous user
            return mapFirebaseUser(authResult.user)
        }
        return mapFirebaseUser(auth.currentUser)
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
        // Only create a profile if one doesn't already exist for this UID
        val userDoc = db.collection("users").document(user.uid).get().await()
        if (!userDoc.exists()) {
            db.collection("users").document(user.uid).set(
                mapOf(
                    "name" to (user.displayName ?: "User"),
                    "profilePic" to user.photoUrl?.toString(),
                    "email" to user.email,
                    "phone" to user.phoneNumber,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
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