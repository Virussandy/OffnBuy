package com.ozonic.offnbuy.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Immutable
@Serializable
@Entity(tableName = "deals")
data class DealItem(
    @PrimaryKey
    val deal_id: String = "",
    val discount: String? = null,
    val image: String? = null,
    val originalPrice: String? = null,
    var posted_on: String? = null,
    val price: String? = null,
    val redirectUrl: String? = null,
    val store: String? = null,
    val title: String? = null,
    val url: String? = null
) 