package com.ozonic.offnbuy.domain.repository

import com.google.firebase.firestore.ListenerRegistration
import com.ozonic.offnbuy.data.local.model.FavoriteDealEntity
import com.ozonic.offnbuy.data.local.model.GeneratedLinkEntity
import com.ozonic.offnbuy.domain.model.User
import com.ozonic.offnbuy.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the user data repository.
 * This repository is responsible for managing all data related to a specific user,
 * such as their profile, favorite deals, and generated links.
 */
interface UserDataRepository {

    /**
     * Gets the currently authenticated user's ID.
     *
     * @return The user ID, or null if no user is signed in.
     */
    fun getCurrentUserId(): String?

    /**
     * Retrieves a user's profile as a Flow, which will automatically update on changes.
     *
     * @param userId The ID of the user whose profile is to be fetched.
     * @return A Flow emitting the [UserProfile], or null if not found.
     */
    fun getUserProfile(userId: String): Flow<UserProfile?>

    /**
     * Sets up a real-time listener for changes to a user's profile in Firestore.
     *
     * @param userId The ID of the user to listen to.
     * @return A [ListenerRegistration] that can be used to cancel the listener.
     */
    fun listenForUserProfileChanges(userId: String): ListenerRegistration

    /**
     * Retrieves a user's favorite deals as a Flow.
     *
     * @param userId The ID of the user.
     * @return A Flow emitting a list of [FavoriteDealEntity].
     */
    fun getFavorites(userId: String): Flow<List<FavoriteDealEntity>>

    /**
     * Sets up a real-time listener for changes to a user's favorite deals.
     *
     * @param userId The ID of the user.
     * @return A [ListenerRegistration] for the listener.
     */
    fun listenForFavoriteChanges(userId: String): ListenerRegistration

    /**
     * Retrieves a user's generated links as a Flow.
     *
     * @param userId The ID of the user.
     * @return A Flow emitting a list of [GeneratedLinkEntity].
     */
    fun getGeneratedLinks(userId: String): Flow<List<GeneratedLinkEntity>>

    /**
     * Sets up a real-time listener for changes to a user's generated links.
     *
     * @param userId The ID of the user.
     * @return A [ListenerRegistration] for the listener.
     */
    fun listenForGeneratedLinkChanges(userId: String): ListenerRegistration

    /**
     * Adds a new generated link to both the local and remote databases.
     *
     * @param userId The ID of the user.
     * @param url The URL of the generated link.
     */
    suspend fun addGeneratedLink(userId: String, url: String)

    /**
     * Migrates all local user data from an old user ID to a new one.
     * This is typically used when an anonymous user signs in.
     *
     * @param fromUid The old user ID.
     * @param toUid The new user ID.
     */
    suspend fun migrateLocalUserData(fromUid: String, toUid: String)

    /**
     * Clears all local user data from the database.
     */
    suspend fun clearAllLocalUserData()
}