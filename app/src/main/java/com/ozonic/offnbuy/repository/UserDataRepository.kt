package com.ozonic.offnbuy.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.ozonic.offnbuy.data.FavoriteDealDao
import com.ozonic.offnbuy.data.GeneratedLinkDao
import com.ozonic.offnbuy.data.UserProfileDao
import com.ozonic.offnbuy.model.FavoriteDeal
import com.ozonic.offnbuy.model.GeneratedLink
import com.ozonic.offnbuy.model.UserProfile
import com.ozonic.offnbuy.util.FirestoreCollections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDataRepository(
    private val favoriteDealDao: FavoriteDealDao,
    private val generatedLinkDao: GeneratedLinkDao,
    private val userProfileDao: UserProfileDao
) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // --- USER PROFILE ---
    fun getUserProfile(userId: String) = userProfileDao.getProfile(userId)

    fun listenForUserProfileChanges(userId: String): ListenerRegistration {
        val docRef = db.collection(FirestoreCollections.USERS).document(userId)
        return docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("UserDataRepo", "Profile listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject<UserProfile>()
                if (profile != null) {
                    scope.launch {
                        // Add the UID from the document ID, as it might not be a field in the document
                        userProfileDao.upsert(profile.copy(uid = snapshot.id))
                    }
                }
            } else {
                Log.d("UserDataRepo", "Current data: null")
            }
        }
    }

    // --- FAVORITES ---
    // ... (existing favorite functions are correct) ...
    fun getFavorites(userId: String) = favoriteDealDao.getFavoritesForUser(userId)

    suspend fun addFavorite(userId: String, dealId: String) {
        val favorite = FavoriteDeal(deal_id = dealId, userId = userId)
        favoriteDealDao.insert(favorite)
        db.collection(FirestoreCollections.USERS).document(userId).collection(FirestoreCollections.FAVORITE_DEALS).document(dealId)
            .set(favorite).await()
    }

    suspend fun removeFavorite(userId: String, dealId: String) {
        val favorite = FavoriteDeal(deal_id = dealId, userId = userId)
        favoriteDealDao.delete(favorite)
        db.collection(FirestoreCollections.USERS).document(userId).collection(FirestoreCollections.FAVORITE_DEALS).document(dealId)
            .delete().await()
    }

    fun listenForFavoriteChanges(userId: String): ListenerRegistration {
        val query = db.collection(FirestoreCollections.USERS).document(userId)
            .collection(FirestoreCollections.FAVORITE_DEALS)

        return query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("UserDataRepo", "Favorites listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            for (docChange in snapshot.documentChanges) {
                scope.launch {
                    val favorite = docChange.document.toObject<FavoriteDeal>()
                    when (docChange.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> favoriteDealDao.insert(favorite)
                        DocumentChange.Type.REMOVED -> favoriteDealDao.delete(favorite)
                    }
                }
            }
        }
    }

    // --- GENERATED LINKS ---
    fun getGeneratedLinks(userId: String) = generatedLinkDao.getRecentLinksForUser(userId)

    suspend fun addGeneratedLink(userId: String, url: String) {
        val link = GeneratedLink(userId = userId, url = url)
        generatedLinkDao.insert(link)
        db.collection(FirestoreCollections.USERS).document(userId).collection(FirestoreCollections.GENERATED_LINKS)
            .add(link).await()
    }

    // ++ RESTORE THIS PAGINATION FUNCTION ++
    suspend fun getGeneratedLinksPaginated(userId: String, page: Int, pageSize: Int): List<GeneratedLink> {
        val offset = page * pageSize
        return generatedLinkDao.getLinksPaginated(userId, pageSize, offset)
    }

    fun listenForGeneratedLinkChanges(userId: String): ListenerRegistration {
        val query = db.collection(FirestoreCollections.USERS).document(userId)
            .collection(FirestoreCollections.GENERATED_LINKS)

        return query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("UserDataRepo", "Links listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            for (docChange in snapshot.documentChanges) {
                scope.launch {
                    val link = docChange.document.toObject<GeneratedLink>()
                    when (docChange.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> generatedLinkDao.insert(link)
                        DocumentChange.Type.REMOVED -> generatedLinkDao.delete(link)
                    }
                }
            }
        }
    }

    // --- MIGRATION & DATA CLEARING ---
    suspend fun migrateLocalUserData(fromUid: String, toUid: String) {
        favoriteDealDao.updateUserId(fromUid, toUid)
        generatedLinkDao.updateUserId(fromUid, toUid)
        userProfileDao.updateUserId(fromUid, toUid)
    }

    suspend fun clearAllLocalUserData() {
        favoriteDealDao.clearAll()
        generatedLinkDao.clearAll()
        userProfileDao.clearAll()
    }
}