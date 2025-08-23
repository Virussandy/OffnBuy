package com.ozonic.offnbuy.repository

import com.ozonic.offnbuy.BuildConfig
import com.ozonic.offnbuy.model.ApiRequest
import com.ozonic.offnbuy.model.ApiResponse
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

class GenerateLinkRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }

    private val apiUrl = BuildConfig.API_URL
    private val apiToken = BuildConfig.API_TOKEN

    suspend fun generateLink(productLink: String): ApiResponse {
        return try {
            client.post(apiUrl) {
                header("Authorization", "Bearer $apiToken")
                contentType(ContentType.Application.Json)
                setBody(ApiRequest(deal = productLink))
            }.body()
        } catch (e: HttpRequestTimeoutException) {
            // Specific error for timeouts
            ApiResponse(error = 1, message = "The connection timed out. Please try again with a stronger internet connection.")
        } catch (e: UnknownHostException) {
            // Specific error for DNS/Host issues (often means no real internet or bad URL)
            ApiResponse(error = 1, message = "Could not reach the server. Please check your internet connection.")
        } catch (e: Exception) {
            // General error, providing more technical detail for debugging
            ApiResponse(error = 1, message = "An unexpected error occurred. It might be a network security issue.")
        }
    }
}