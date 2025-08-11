package com.ozonic.offnbuy.repository

import com.ozonic.offnbuy.model.ApiRequest
import com.ozonic.offnbuy.model.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.ozonic.offnbuy.BuildConfig

class GenerateLinkRepository{
    private val client = HttpClient(Android){
        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val apiUrl = BuildConfig.API_URL
    private val apiToken = BuildConfig.API_TOKEN
    suspend fun generateLink(productLink: String): ApiResponse{
        return try{
            val response = client.post(apiUrl){
                header("Authorization", "Bearer $apiToken")
                contentType(ContentType.Application.Json)
                setBody(ApiRequest(deal = productLink))
            }
            response.body()
        }catch (e: Exception){
            ApiResponse(error = 1, message = e.message)
        }
    }
}