package com.ozonic.offnbuy.data.local.model

import androidx.room.Entity
import java.util.Date

/**
 * Represents a favorite deal in the local database.
 *
 * @property deal_id The unique identifier for the deal.
 * @property userId The unique identifier for the user who favorited the deal.
 * @property addedAt The timestamp when the deal was favorited.
 */
@Entity(tableName = "favorite_deals", primaryKeys = ["deal_id", "userId"])
data class FavoriteDealEntity(
    val deal_id: String = "", // Added default value
    val userId: String = "", // Added default value
    val addedAt: Date? = null // Added default value
)