package com.ozonic.offnbuy.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "notified_deals") // Add this annotation
data class NotifiedDeal(
    @PrimaryKey
    val deal_id: String = "",
    @Embedded(prefix = "deal_") // Use @Embedded to store the DealItem fields directly
    val deal: DealItem = DealItem(),
    val isSeen: Boolean = false,
    val timestamp: Long = 0L,
)