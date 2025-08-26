package com.ozonic.offnbuy.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ozonic.offnbuy.domain.model.SupportedStore

/**
 * Represents a supported store in the local database.
 *
 * @property name The name of the store.
 * @property logoUrl The URL of the store's logo.
 */
@Entity(tableName = "supported_stores")
data class SupportedStoreEntity(
    @PrimaryKey
    val name: String = "", // Added default value
    val logoUrl: String = "" // Added default value
) {
    /**
     * Converts this entity to a [SupportedStore] domain model.
     *
     * @return The [SupportedStore] domain model.
     */
    fun toDomainModel(): SupportedStore {
        return SupportedStore(
            name = name,
            logoUrl = logoUrl
        )
    }
}