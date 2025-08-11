package com.ozonic.offnbuy.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiRequest(
    val deal: String,
    val convert_option: String = "convert_Only"
)