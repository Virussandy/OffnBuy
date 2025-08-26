package com.ozonic.offnbuy.domain.repository

import com.ozonic.offnbuy.domain.model.NotifiedDeal
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the notification repository.
 * This repository is responsible for managing all notification-related data,
 * including listening for new notifications and managing their read/seen status.
 */
interface NotificationRepository {

    /**
     * Returns a stream of all notified deals from the local database.
     * This flow will automatically update when the notification data changes.
     *
     * @return A [Flow] of a list of [NotifiedDeal] objects.
     */
    fun getNotifiedDealsStream(): Flow<List<NotifiedDeal>>

    /**
     * Starts a listener for new notifications from the remote data source (Firebase Realtime Database).
     * New notifications are then fetched and stored locally.
     */
    fun startListeningForNotifications()

    /**
     * Marks a specific notification as "seen" by its deal ID.
     * This typically involves updating its status in a local storage mechanism like SharedPreferences.
     *
     * @param dealId The unique identifier of the deal associated with the notification to be marked as seen.
     */
    suspend fun markAsSeen(dealId: String)

    /**
     * Marks a list of notifications as "seen".
     *
     * @param dealIds A list of deal IDs to be marked as seen.
     */
    suspend fun markAllAsSeen(dealIds: List<String>)
}