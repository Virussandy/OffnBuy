package com.ozonic.offnbuy.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ozonic.offnbuy.domain.model.Deal

/**
 * Represents a deal in the local database.
 *
 * @property deal_id The unique identifier for the deal.
 * @property discount The discount percentage or amount.
 * @property image The URL of the deal's image.
 * @property originalPrice The original price of the item.
 * @property posted_on The timestamp when the deal was posted.
 * @property price The discounted price of the item.
 * @property redirectUrl The URL to redirect to for the deal.
 * @property store The name of the store offering the deal.
 * @property title The title of the deal.
 * @property url The URL of the deal.
 */
@Entity(tableName = "deals")
data class DealEntity(
    @PrimaryKey
    val deal_id: String = "", // Added default value
    val discount: String? = null, // Added default value
    val image: String? = null, // Added default value
    val originalPrice: String? = null, // Added default value
    val posted_on: String? = null, // Added default value
    val price: String? = null, // Added default value
    val redirectUrl: String? = null, // Added default value
    val store: String? = null, // Added default value
    val title: String? = null, // Added default value
    val url: String? = null // Added default value
) {
    /**
     * Converts this entity to a [Deal] domain model.
     *
     * @return The [Deal] domain model.
     */
    fun toDomainModel(): Deal {
        return Deal(
            deal_id = deal_id,
            discount = discount,
            image = image,
            originalPrice = originalPrice,
            posted_on = posted_on,
            price = price,
            redirectUrl = redirectUrl,
            store = store,
            title = title,
            url = url
        )
    }
}