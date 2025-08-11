package com.ozonic.offnbuy.model

import androidx.compose.runtime.Immutable

@Immutable
data class NotifiedDeal(
    val deal: DealItem = DealItem(),
    val isSeen: Boolean = false,
    val deal_id: String = "",
    val timestamp: Long = 0L,
)

data class NotificationItem(
    val deal: DealItem = DealItem(),
    val timestamp: Long = 0L,
)