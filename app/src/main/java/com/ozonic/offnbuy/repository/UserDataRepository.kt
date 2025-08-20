package com.ozonic.offnbuy.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.ozonic.offnbuy.data.FavoriteDealDao
import com.ozonic.offnbuy.data.GeneratedLinkDao
import com.ozonic.offnbuy.model.FavoriteDeal
import com.ozonic.offnbuy.model.GeneratedLink
import com.ozonic.offnbuy.util.FirestoreCollections
import kotlinx.coroutines.tasks.await

class UserDataRepository(
    private val favoriteDealDao: FavoriteDealDao,
    private val generatedLinkDao: GeneratedLinkDao
) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String?
        get() = auth.currentUser?.uid

    // --- FAVORITE DEALS ---

    fun getFavorites() = favoriteDealDao.getFavoritesForUser(userId ?: "")

    suspend fun addFavorite(dealId: String) {
        val uid = userId ?: return
        val favorite = FavoriteDeal(deal_id = dealId, userId = uid, addedAt = java.util.Date())

        // 1. Write to local cache immediately for instant UI update
        favoriteDealDao.insert(favorite)

        // 2. Write to Firestore for cloud backup, using the server's timestamp
        val firestoreFavorite = mapOf(
            "deal_id" to dealId,
            "userId" to uid,
            "addedAt" to FieldValue.serverTimestamp() // Use server time
        )
        db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.FAVORITE_DEALS).document(dealId)
            .set(firestoreFavorite).await()
    }

    suspend fun removeFavorite(dealId: String) {
        val uid = userId ?: return
        val favorite = FavoriteDeal(deal_id = dealId, userId = uid)

        // 1. Remove from local cache immediately
        favoriteDealDao.delete(favorite)

        // 2. Remove from Firestore
        db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.FAVORITE_DEALS).document(dealId)
            .delete().await()
    }

    /**
     * Synchronizes favorites from Firestore to the local Room database.
     * This is called when a user logs in.
     */
    suspend fun syncFavorites() {
        val uid = userId ?: return
        val snapshot = db.collection(FirestoreCollections.USERS).document(uid).collection(
            FirestoreCollections.FAVORITE_DEALS).get().await()
        val firestoreFavorites = snapshot.documents.mapNotNull { it.toObject<FavoriteDeal>() }

        // Clear local cache and insert fresh data from the cloud
        favoriteDealDao.clearFavoritesForUser(uid)
        firestoreFavorites.forEach { favoriteDealDao.insert(it) }
    }


    // --- GENERATED LINKS ---

    fun getGeneratedLinks() = generatedLinkDao.getRecentLinksForUser(userId ?: "")

    suspend fun addGeneratedLink(url: String) {
        val uid = userId ?: return

        val querySnapshot = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.GENERATED_LINKS)
            .whereEqualTo("url", url)
            .limit(1) // We only need to know if at least one exists.
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            return // Exit the function to prevent saving a duplicate.
        }

        val link = GeneratedLink(url = url, userId = uid, createdAt = java.util.Date())

        // 1. Write to local cache
        generatedLinkDao.insert(link)

        // 2. Write to Firestore
        val firestoreLink = mapOf(
            "url" to url,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp() // Use server time
        )
        // Using url as the document ID for simplicity and to prevent duplicates
        db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.GENERATED_LINKS).document()
            .set(firestoreLink).await()
    }

    // Add this new function to the class
    suspend fun getGeneratedLinksPaginated(page: Int, pageSize: Int = 10): List<GeneratedLink> {
        val uid = userId ?: return emptyList()
        val offset = page * pageSize
        return generatedLinkDao.getLinksPaginated(uid, pageSize, offset)
    }

    suspend fun syncGeneratedLinks() {
        val uid = userId ?: return
        val snapshot = db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.GENERATED_LINKS).get().await()
        val firestoreLinks = snapshot.documents.mapNotNull { it.toObject<GeneratedLink>() }

        generatedLinkDao.clearLinksForUser(uid)
        firestoreLinks.forEach { generatedLinkDao.insert(it) }
    }


    // --- DATA CLEARING ---

    /**
     * Clears all user-specific data from the local database for a specific user ID.
     * This is crucial to call on logout or when switching users.
     */
    suspend fun clearAllLocalDataForUser(userId: String) { // <-- MODIFIED
        favoriteDealDao.clearFavoritesForUser(userId)
        generatedLinkDao.clearLinksForUser(userId)
    }

    suspend fun clearAllLocalDataForUser() {
        val uid = userId ?: return
        clearAllLocalDataForUser(uid)
    }
}