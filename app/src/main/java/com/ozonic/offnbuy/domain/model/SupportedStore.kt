package com.ozonic.offnbuy.domain.model

/**
 * Represents a supported store in the domain layer.
 *
 * @property name The name of the store.
 * @property logoUrl The URL of the store's logo.
 */
data class SupportedStore(
    val name: String = "",
    val logoUrl: String = ""
)