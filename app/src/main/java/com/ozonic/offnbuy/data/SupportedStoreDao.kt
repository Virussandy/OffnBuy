package com.ozonic.offnbuy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.model.SupportedStore
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportedStoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stores: List<SupportedStore>)

    @Query("SELECT * FROM supported_stores ORDER BY name ASC")
    fun getAll(): Flow<List<SupportedStore>>

    @Query("DELETE FROM supported_stores")
    suspend fun clearAll()
}