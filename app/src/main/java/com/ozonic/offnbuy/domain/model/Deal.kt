package com.ozonic.offnbuy.domain.model

/**
 * Represents a deal in the domain layer.
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
data class Deal(
    val deal_id: String,
    val discount: String?,
    val image: String?,
    val originalPrice: String?,
    val posted_on: String?,
    val price: String?,
    val redirectUrl: String?,
    val store: String?,
    val title: String?,
    val url: String?
)