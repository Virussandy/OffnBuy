package com.ozonic.offnbuy.data.repository

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ozonic.offnbuy.domain.model.User
import com.ozonic.offnbuy.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepositoryImpl : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private fun mapFirebaseUser(firebaseUser: FirebaseUser?): User? {
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

    override fun getAuthStateStream(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(mapFirebaseUser(firebaseAuth.currentUser))
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    override fun startPhoneNumberVerification(
        phone: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): User {
        val result = auth.signInWithCredential(credential).await()
        return mapFirebaseUser(result.user) ?: throw IllegalStateException("Sign-in failed to return a user.")
    }

    override suspend fun refreshUser() {
        auth.currentUser?.reload()?.await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    // The user profile creation is now handled by the ViewModel, but the update logic remains here.
    override suspend fun updateDisplayName(displayName: String) {
        val user = auth.currentUser ?: return
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
        user.updateProfile(profileUpdates).await()
        db.collection("users").document(user.uid).update("name", displayName).await()
    }

    override suspend fun updateProfilePicture(uri: Uri) {
        val user = auth.currentUser ?: return
        val storageRef = storage.reference.child("profile_pictures/${user.uid}")
        val downloadUrl = storageRef.putFile(uri).await().storage.downloadUrl.await()
        val profileUpdates = UserProfileChangeRequest.Builder().setPhotoUri(downloadUrl).build()
        user.updateProfile(profileUpdates).await()
        db.collection("users").document(user.uid).update("profilePic", downloadUrl.toString()).await()
    }

    override suspend fun verifyBeforeUpdateEmail(email: String) {
        val user = auth.currentUser ?: return
        user.verifyBeforeUpdateEmail(email).await()
        db.collection("users").document(user.uid).update("email", email).await()
    }

    override suspend fun updatePhoneNumber(credential: PhoneAuthCredential) {
        val user = auth.currentUser ?: return
        user.updatePhoneNumber(credential).await()
        db.collection("users").document(user.uid).update("phone", auth.currentUser?.phoneNumber).await()
    }
}