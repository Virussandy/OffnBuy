package com.ozonic.offnbuy.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.ozonic.offnbuy.data.local.dao.FavoriteDealDao
import com.ozonic.offnbuy.data.local.dao.GeneratedLinkDao
import com.ozonic.offnbuy.data.local.dao.UserProfileDao
import com.ozonic.offnbuy.data.local.model.FavoriteDealEntity
import com.ozonic.offnbuy.data.local.model.GeneratedLinkEntity
import com.ozonic.offnbuy.data.local.model.UserProfileEntity
import com.ozonic.offnbuy.domain.model.User
import com.ozonic.offnbuy.domain.model.UserProfile
import com.ozonic.offnbuy.domain.repository.UserDataRepository
import com.ozonic.offnbuy.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Implementation of the [UserDataRepository] interface.
 * This class is responsible for managing all data related to a specific user,
 * such as their profile, favorite deals, and generated links. It coordinates
 * data between the local Room database and the remote Firestore database.
 */
class UserDataRepositoryImpl(
    private val favoriteDealDao: FavoriteDealDao,
    private val generatedLinkDao: GeneratedLinkDao,
    private val userProfileDao: UserProfileDao
) : UserDataRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun getUserProfile(userId: String): Flow<UserProfile?> {
        return userProfileDao.getProfile(userId).map { it?.toDomainModel() }
    }

    override fun listenForUserProfileChanges(userId: String): ListenerRegistration {
        val docRef = db.collection(Constants.USERS).document(userId)
        return docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject<UserProfileEntity>()
                if (profile != null) {
                    scope.launch {
                        userProfileDao.upsert(profile.copy(uid = snapshot.id))
                    }
                }
            }
        }
    }

    override fun getFavorites(userId: String): Flow<List<FavoriteDealEntity>> {
        return favoriteDealDao.getFavoritesForUser(userId)
    }

    override fun listenForFavoriteChanges(userId: String): ListenerRegistration {
        val query = db.collection(Constants.USERS).document(userId)
            .collection(Constants.FAVORITE_DEALS)

        return query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            for (docChange in snapshot.documentChanges) {
                scope.launch {
                    val favorite = docChange.document.toObject<FavoriteDealEntity>()
                    when (docChange.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> favoriteDealDao.insert(favorite)
                        DocumentChange.Type.REMOVED -> favoriteDealDao.delete(favorite)
                    }
                }
            }
        }
    }

    override fun getGeneratedLinks(userId: String): Flow<List<GeneratedLinkEntity>> {
        return generatedLinkDao.getRecentLinksForUser(userId)
    }

    override fun listenForGeneratedLinkChanges(userId: String): ListenerRegistration {
        val query = db.collection(Constants.USERS).document(userId)
            .collection(Constants.GENERATED_LINKS)

        return query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            for (docChange in snapshot.documentChanges) {
                scope.launch {
                    val link = docChange.document.toObject<GeneratedLinkEntity>()
                    when (docChange.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> generatedLinkDao.insert(link)
                        DocumentChange.Type.REMOVED -> generatedLinkDao.delete(link)
                    }
                }
            }
        }
    }

    override suspend fun addGeneratedLink(userId: String, url: String) {
        val link = GeneratedLinkEntity(userId = userId, url = url, createdAt = null)
        generatedLinkDao.insert(link)
        db.collection(Constants.USERS).document(userId).collection(Constants.GENERATED_LINKS)
            .add(link).await()
    }

    override suspend fun migrateLocalUserData(fromUid: String, toUid: String) {
        favoriteDealDao.updateUserId(fromUid, toUid)
        generatedLinkDao.updateUserId(fromUid, toUid)
        userProfileDao.updateUserId(fromUid, toUid)
    }

    override suspend fun clearAllLocalUserData() {
        favoriteDealDao.clearAll()
        generatedLinkDao.clearAll()
        userProfileDao.clearAll()
    }
}