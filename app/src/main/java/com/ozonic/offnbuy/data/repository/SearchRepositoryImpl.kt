package com.ozonic.offnbuy.data.repository

import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchForHits
import com.algolia.client.model.search.SearchMethodParams
import com.algolia.client.model.search.SearchResponse
import com.ozonic.offnbuy.BuildConfig
import com.ozonic.offnbuy.data.local.model.DealEntity
import com.ozonic.offnbuy.data.remote.dto.DealDto
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Implementation of the [SearchRepository] interface.
 * Handles searching for deals using the Algolia API.
 */
class SearchRepositoryImpl : SearchRepository {
    private val appId = BuildConfig.APP_ID
    private val apiKey = BuildConfig.API_KEY
    private val indexName = BuildConfig.INDEX_NAME

    private val client = SearchClient(appId = appId, apiKey = apiKey)
    private val ioDispatcher = Dispatchers.IO

    override suspend fun searchDeals(searchText: String, page: Int): Pair<List<Deal>, Boolean> {
        return withContext(ioDispatcher) {
            if (searchText.isBlank()) {
                return@withContext Pair(emptyList(), false)
            }
            try {
                val result = client.search(
                    searchMethodParams = SearchMethodParams(
                        requests = listOf(
                            SearchForHits(
                                indexName = indexName,
                                query = searchText,
                                page = page
                            )
                        )
                    )
                ).results.first()
                val dealItems: List<Deal> = mapHitsToDealItems(result as SearchResponse)
                val hasMore = ((result.page ?: 0) + 1) < (result.nbPages ?: 0)
                Pair(dealItems, hasMore)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(emptyList(), false)
            }
        }
    }

    private fun mapHitsToDealItems(response: SearchResponse): List<Deal> {
        return response.hits.mapNotNull { hit ->
            val props = hit.additionalProperties
            try {
                Deal(
                    deal_id = props?.get("deal_id")?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    discount = props["discount"]?.jsonPrimitive?.contentOrNull ?: "",
                    image = props["image"]?.jsonPrimitive?.contentOrNull ?: "",
                    originalPrice = props["originalPrice"]?.jsonPrimitive?.contentOrNull ?: "",
                    posted_on = props["posted_on"]?.jsonPrimitive?.contentOrNull ?: "",
                    price = props["price"]?.jsonPrimitive?.contentOrNull ?: "",
                    redirectUrl = props["redirectUrl"]?.jsonPrimitive?.contentOrNull ?: "",
                    store = props["store"]?.jsonPrimitive?.contentOrNull ?: "",
                    title = props["title"]?.jsonPrimitive?.contentOrNull ?: "",
                    url = props["url"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}