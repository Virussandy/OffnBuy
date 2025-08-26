package com.ozonic.offnbuy.data.remote.service

import com.ozonic.offnbuy.BuildConfig
import com.ozonic.offnbuy.data.remote.dto.ApiRequest
import com.ozonic.offnbuy.data.remote.dto.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.net.UnknownHostException

/**
 * Service for interacting with the link generation API.
 */
class LinkApiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }

    private val apiUrl = BuildConfig.API_URL
    private val apiToken = BuildConfig.API_TOKEN

    /**
     * Generates an affiliate link from a product URL.
     *
     * @param productLink The product URL.
     * @return An [ApiResponse] containing the generated link or an error.
     */
    suspend fun generateLink(productLink: String): ApiResponse {
        return try {
            client.post(apiUrl) {
                header("Authorization", "Bearer $apiToken")
                contentType(ContentType.Application.Json)
                setBody(ApiRequest(deal = productLink))
            }.body()
        } catch (e: HttpRequestTimeoutException) {
            ApiResponse(error = 1, message = "The connection timed out.")
        } catch (e: UnknownHostException) {
            ApiResponse(error = 1, message = "Could not reach the server. Check your internet connection.")
        } catch (e: Exception) {
            ApiResponse(error = 1, message = "An unexpected error occurred.")
        }
    }
}