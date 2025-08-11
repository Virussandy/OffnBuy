package com.ozonic.offnbuy.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success: Int? = null,
    val data:String? = null,
    val error: Int? = null,
    val message: String? = null
)