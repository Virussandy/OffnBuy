package com.ozonic.offnbuy.domain.model

/**
 * Represents a notified deal in the domain layer.
 *
 * @property deal The deal information.
 * @property isSeen Whether the notification has been seen by the user.
 * @property deal_id The unique identifier for the deal.
 * @property timestamp The timestamp of the notification.
 */
data class NotifiedDeal(
    val deal: Deal,
    val isSeen: Boolean,
    val deal_id: String,
    val timestamp: Long
)