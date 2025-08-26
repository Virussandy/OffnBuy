package com.ozonic.offnbuy.domain.usecase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.repository.DealsRepository
import kotlinx.coroutines.flow.Flow

/**
 * A use case for fetching and observing deals.
 *
 * @param dealsRepository The repository for deals.
 */
class GetDealsUseCase(private val dealsRepository: DealsRepository) {

    /**
     * Returns a stream of deals from the local database.
     *
     * @return A [Flow] of a list of [Deal] objects.
     */
    fun execute(): Flow<List<Deal>> {
        return dealsRepository.getDealsStream()
    }

    /**
     * Syncs new deals from the remote server.
     *
     * @param lastVisible The last visible document for pagination.
     * @return The next last visible document, or null if there are no more.
     */
    suspend fun sync(lastVisible: DocumentSnapshot?): DocumentSnapshot? {
        return dealsRepository.syncDeals(lastVisible)
    }

    /**
     * Forces a refresh of the newest deals.
     */
    suspend fun refresh() {
        dealsRepository.syncNewestDeals()
    }

    /**
     * Listens for real-time changes to new deals.
     *
     * @param latestDealTimestamp The timestamp of the latest deal.
     * @param onNewDeal The callback to be invoked when a new deal is found.
     * @return A [ListenerRegistration] for managing the listener.
     */
    fun listenForNewDeals(latestDealTimestamp: String, onNewDeal: (Deal) -> Unit): ListenerRegistration? {
        return dealsRepository.listenForNewDeals(latestDealTimestamp, onNewDeal)
    }
}