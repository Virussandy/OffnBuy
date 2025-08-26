package com.ozonic.offnbuy.domain.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.ozonic.offnbuy.domain.model.Deal
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the deals repository.
 * This repository is responsible for fetching, caching, and streaming deal data,
 * abstracting the data sources from the rest of the application.
 */
interface DealsRepository {

    /**
     * Returns a stream of deals from the local database. This flow will automatically
     * update whenever the underlying data changes.
     *
     * @return A [Flow] of a list of [Deal] objects.
     */
    fun getDealsStream(): Flow<List<Deal>>

    /**
     * Synchronizes deals from the remote Firestore database to the local Room database.
     * This method supports pagination.
     *
     * @param lastVisible The last visible [DocumentSnapshot] from the previous fetch, used for pagination.
     * @return The next [DocumentSnapshot] to be used for the next page, or null if there are no more pages.
     */
    suspend fun syncDeals(lastVisible: DocumentSnapshot?): DocumentSnapshot?

    /**
     * Fetches a single deal directly from Firestore by its ID.
     * This is useful for fetching deals that might not be in the local cache,
     * such as when opening a deal from a notification.
     *
     * @param dealId The unique identifier of the deal to fetch.
     * @return The [Deal] object, or null if it's not found.
     */
    suspend fun getDealFromFirestore(dealId: String): Deal?

    /**
     * Fetches the most recent deals from Firestore and updates the local database.
     * This is typically used for a "pull-to-refresh" action.
     */
    suspend fun syncNewestDeals()

    /**
     * Sets up a real-time listener for new deals in Firestore that are newer
     * than the latest one stored locally.
     *
     * @param latestDealTimestamp The timestamp of the most recent deal in the local database.
     * @param onNewDeal A callback function that is invoked when a new deal is detected.
     * @return A [ListenerRegistration] that can be used to remove the listener when it's no longer needed.
     */
    fun listenForNewDeals(latestDealTimestamp: String, onNewDeal: (Deal) -> Unit): ListenerRegistration?
}