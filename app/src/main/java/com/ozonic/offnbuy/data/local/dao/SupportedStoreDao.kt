package com.ozonic.offnbuy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.data.local.model.SupportedStoreEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the supported_stores table.
 */
@Dao
interface SupportedStoreDao {

    /**
     * Inserts a list of supported stores into the database, replacing any existing entries with the same name.
     *
     * @param stores The list of supported stores to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stores: List<SupportedStoreEntity>)

    /**
     * Gets a flow of all supported stores from the database, ordered by name.
     *
     * @return A [Flow] of a list of [SupportedStoreEntity] objects.
     */
    @Query("SELECT * FROM supported_stores ORDER BY name ASC")
    fun getAll(): Flow<List<SupportedStoreEntity>>

    /**
     * Deletes all supported stores from the database.
     */
    @Query("DELETE FROM supported_stores")
    suspend fun clearAll()
}