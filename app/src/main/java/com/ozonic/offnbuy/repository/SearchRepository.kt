package com.ozonic.offnbuy.repository

import android.util.Log
import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchForHits
import com.algolia.client.model.search.SearchMethodParams
import com.algolia.client.model.search.SearchResponse
import com.ozonic.offnbuy.model.DealItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import com.ozonic.offnbuy.BuildConfig

class SearchRepository {
    val appId = BuildConfig.APP_ID
    val apiKey = BuildConfig.API_KEY
    val indexName = BuildConfig.INDEX_NAME

    val client = SearchClient(appId = appId, apiKey = apiKey)
    private val ioDispatcher = Dispatchers.IO

    suspend fun searchDeals(
        searchText: String,
        page: Int = 0 // Add page parameter for pagination
    ): Pair<List<DealItem>, Boolean> {
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
                val dealItem: List<DealItem> = mapHitsToDealItems(result as SearchResponse)
                val hasMore = ((result.page ?: 0) + 1) < (result.nbPages ?: 0)
                Log.d("DealsRepository", "Search result: $result")
                Pair(dealItem, hasMore)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(emptyList(), false)
            }
        }
    }

    fun mapHitsToDealItems(response: SearchResponse): List<DealItem> {
        return response.hits.mapNotNull { hit ->
            val props = hit.additionalProperties

            try {
                DealItem(
                    deal_id = props?.get("deal_id")?.jsonPrimitive?.contentOrNull
                        ?: return@mapNotNull null,
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