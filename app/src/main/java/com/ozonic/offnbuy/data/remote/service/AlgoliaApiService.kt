package com.ozonic.offnbuy.data.remote.service

import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchForHits
import com.algolia.client.model.search.SearchMethodParams
import com.algolia.client.model.search.SearchResponse
import com.ozonic.offnbuy.BuildConfig
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Service for interacting with the Algolia search API.
 */
class AlgoliaApiService {
    private val appId = BuildConfig.APP_ID
    private val apiKey = BuildConfig.API_KEY
    private val indexName = BuildConfig.INDEX_NAME

    private val client = SearchClient(appId = appId, apiKey = apiKey)

    /**
     * Searches for deals based on a query.
     *
     * @param query The search query.
     * @param page The page number to retrieve.
     * @return A [SearchResponse] containing the search results.
     */
    suspend fun search(query: String, page: Int): SearchResponse {
        return client.search(
            searchMethodParams = SearchMethodParams(
                requests = listOf(
                    SearchForHits(
                        indexName = indexName,
                        query = query,
                        page = page
                    )
                )
            )
        ).results.first() as SearchResponse
    }
}