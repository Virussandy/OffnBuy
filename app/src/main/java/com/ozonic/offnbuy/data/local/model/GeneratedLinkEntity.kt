package com.ozonic.offnbuy.data.local.model

import androidx.room.Entity
import java.util.Date

/**
 * Represents a generated link in the local database.
 *
 * @property url The generated URL.
 * @property userId The unique identifier for the user who generated the link.
 * @property createdAt The timestamp when the link was generated.
 */
@Entity(tableName = "generated_links", primaryKeys = ["url", "userId"])
data class GeneratedLinkEntity(
    val url: String = "", // Added default value
    val userId: String = "", // Added default value
    val createdAt: Date? = null // Added default value
)