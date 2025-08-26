package com.ozonic.offnbuy.data.local.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ozonic.offnbuy.domain.model.NotifiedDeal

/**
 * Represents a notified deal in the local database.
 *
 * @property deal_id The unique identifier for the deal.
 * @property deal The deal information.
 * @property isSeen Whether the notification has been seen by the user.
 * @property timestamp The timestamp of the notification.
 */
@Entity(tableName = "notified_deals")
data class NotifiedDealEntity(
    @PrimaryKey
    val deal_id: String = "", // Added default value
    @Embedded(prefix = "deal_")
    val deal: DealEntity = DealEntity(), // Added default value
    val isSeen: Boolean = false, // Added default value
    val timestamp: Long = 0L, // Added default value
) {
    /**
     * Converts this entity to a [NotifiedDeal] domain model.
     *
     * @return The [NotifiedDeal] domain model.
     */
    fun toDomainModel(): NotifiedDeal {
        return NotifiedDeal(
            deal = deal.toDomainModel(),
            isSeen = isSeen,
            deal_id = deal_id,
            timestamp = timestamp
        )
    }
}