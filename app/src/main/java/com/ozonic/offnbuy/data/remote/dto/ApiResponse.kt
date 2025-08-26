package com.ozonic.offnbuy.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Represents the response from the link generation API.
 *
 * @property success Indicates if the request was successful.
 * @property data The generated link or an error message.
 * @property error Indicates if an error occurred.
 * @property message An optional error message.
 */
@Serializable
data class ApiResponse(
    val success: Int? = null,
    val data: String? = null,
    val error: Int? = null,
    val message: String? = null
)