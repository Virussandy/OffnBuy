package com.ozonic.offnbuy.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Represents the request body for generating a link.
 *
 * @property deal The URL of the deal to be converted.
 * @property convert_option The conversion option for the link.
 */
@Serializable
data class ApiRequest(
    val deal: String,
    val convert_option: String = "convert_Only"
)