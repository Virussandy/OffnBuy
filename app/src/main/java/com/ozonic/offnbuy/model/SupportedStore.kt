package com.ozonic.offnbuy.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supported_stores")
data class SupportedStore(
    @PrimaryKey
    val name: String = "",
    val logoUrl: String = ""
)