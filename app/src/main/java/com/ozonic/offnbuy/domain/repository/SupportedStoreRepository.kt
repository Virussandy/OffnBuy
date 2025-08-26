package com.ozonic.offnbuy.domain.repository

import com.ozonic.offnbuy.domain.model.SupportedStore
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the supported stores repository.
 * This repository is responsible for fetching and caching the list of supported stores.
 */
interface SupportedStoreRepository {

    /**
     * Returns a stream of supported stores from the local database.
     *
     * @return A [Flow] of a list of [SupportedStore] objects.
     */
    fun getStores(): Flow<List<SupportedStore>>

    /**
     * Synchronizes the list of supported stores from the remote Firestore
     * database to the local Room database.
     */
    suspend fun syncStores()
}