package com.ozonic.offnbuy.domain.usecase

import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.repository.SearchRepository

/**
 * A use case for searching deals.
 *
 * @param searchRepository The repository for searching deals.
 */
class SearchDealsUseCase(private val searchRepository: SearchRepository) {

    /**
     * Searches for deals based on a query with pagination.
     *
     * @param query The search query.
     * @param page The page number for pagination.
     * @return A pair of a list of [Deal] objects and a boolean indicating if there are more items.
     */
    suspend fun execute(query: String, page: Int): Pair<List<Deal>, Boolean> {
        return searchRepository.searchDeals(query, page)
    }
}