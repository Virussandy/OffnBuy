package com.ozonic.offnbuy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ozonic.offnbuy.data.local.model.NotifiedDealEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the notified_deals table.
 */
@Dao
interface NotifiedDealDao {

    /**
     * Inserts or updates a notified deal in the database.
     *
     * @param notifiedDeal The notified deal to insert or update.
     */
    @Upsert
    suspend fun upsert(notifiedDeal: NotifiedDealEntity)

    /**
     * Inserts or updates a list of notified deals in the database.
     *
     * @param deals The list of notified deals to insert or update.
     */
    @Upsert
    suspend fun upsertAll(deals: List<NotifiedDealEntity>)

    /**
     * Gets a flow of all notified deals from the database, ordered by timestamp.
     *
     * @return A [Flow] of a list of [NotifiedDealEntity] objects.
     */
    @Query("SELECT * FROM notified_deals ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotifiedDealEntity>>

    /**
     * Deletes a notified deal from the database by its ID.
     *
     * @param dealId The ID of the notified deal to delete.
     */
    @Query("DELETE FROM notified_deals WHERE deal_id = :dealId")
    suspend fun delete(dealId: String)

    /**
     * Deletes all notified deals from the database.
     */
    @Query("DELETE FROM notified_deals")
    suspend fun clearAll()
}