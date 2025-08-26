package com.ozonic.offnbuy.data.repository

import com.ozonic.offnbuy.BuildConfig
import com.ozonic.offnbuy.data.remote.dto.ApiRequest
import com.ozonic.offnbuy.data.remote.dto.ApiResponse
import com.ozonic.offnbuy.domain.repository.LinkRepository
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
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
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.net.UnknownHostException

/**
 * Implementation of the [LinkRepository] interface.
 * Handles network calls for generating affiliate links.
 */
class LinkRepositoryImpl(private val connectivityObserver: NetworkConnectivityObserver) : LinkRepository {

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

    override suspend fun generateLink(productLink: String): ApiResponse {
        if (!connectivityObserver.observe().first()) {
            return ApiResponse(error = 1, message = "No internet connection. Please check your network and try again.")
        }
        return try {
            client.post(apiUrl) {
                header("Authorization", "Bearer $apiToken")
                contentType(ContentType.Application.Json)
                setBody(ApiRequest(deal = productLink))
            }.body()
        } catch (e: HttpRequestTimeoutException) {
            ApiResponse(error = 1, message = "The connection timed out. Please try again with a stronger internet connection.")
        } catch (e: UnknownHostException) {
            ApiResponse(error = 1, message = "Could not reach the server. Please check your internet connection.")
        } catch (e: Exception) {
            ApiResponse(error = 1, message = "An unexpected error occurred. It might be a network security issue.")
        }
    }
}