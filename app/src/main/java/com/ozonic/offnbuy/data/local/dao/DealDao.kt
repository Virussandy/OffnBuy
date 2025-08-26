package com.ozonic.offnbuy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.data.local.model.DealEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the deals table.
 */
@Dao
interface DealDao {

    /**
     * Inserts a list of deals into the database, replacing any existing deals with the same ID.
     *
     * @param deals The list of deals to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(deals: List<DealEntity>)

    /**
     * Gets a flow of all deals from the database, ordered by posted date.
     *
     * @return A [Flow] of a list of [DealEntity] objects.
     */
    @Query("SELECT * FROM deals ORDER BY posted_on DESC LIMIT 5000")
    fun getDealsSteam(): Flow<List<DealEntity>>

    /**
     * Gets a deal by its ID.
     *
     * @param dealId The ID of the deal to retrieve.
     * @return The [DealEntity] object, or null if not found.
     */
    @Query("SELECT * FROM deals WHERE deal_id = :dealId")
    suspend fun getDealById(dealId: String): DealEntity?

    /**
     * Deletes the oldest deals if the number of deals in the database exceeds the limit.
     */
    @Query("DELETE FROM deals WHERE deal_id IN (SELECT deal_id FROM deals ORDER BY posted_on ASC LIMIT -1 OFFSET 5000)")
    suspend fun deleteOldestIfExceedsLimit()

    /**
     * Deletes all deals from the database.
     */
    @Query("DELETE FROM deals")
    suspend fun clearAll()
}