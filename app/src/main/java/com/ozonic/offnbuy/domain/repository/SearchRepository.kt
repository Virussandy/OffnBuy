package com.ozonic.offnbuy.domain.repository

import com.ozonic.offnbuy.domain.model.Deal

/**
 * Interface for the search repository.
 * This repository is responsible for providing search functionality for deals,
 * abstracting the underlying search engine (e.g., Algolia) from the application.
 */
interface SearchRepository {

    /**
     * Searches for deals based on a text query with pagination.
     *
     * @param searchText The text to search for within the deals.
     * @param page The page number of the search results to retrieve.
     * @return A [Pair] containing a list of [Deal] objects that match the search query
     * and a [Boolean] indicating whether there are more pages of results available.
     */
    suspend fun searchDeals(searchText: String, page: Int): Pair<List<Deal>, Boolean>
}