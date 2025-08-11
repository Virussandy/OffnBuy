package com.ozonic.offnbuy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.model.DealItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(deals: List<DealItem>)

    @Query("SELECT * FROM deals ORDER BY posted_on DESC LIMIT 5000")
    fun getDealsSteam(): Flow<List<DealItem>>

    @Query("SELECT * FROM deals WHERE deal_id = :dealId")
    suspend fun getDealById(dealId: String): DealItem?

    @Query("DELETE FROM deals WHERE deal_id IN (SELECT deal_id FROM deals ORDER BY posted_on ASC LIMIT -1 OFFSET 5000)")
    suspend fun deleteOldestIfExceedsLimit()

    @Query("DELETE FROM deals")
    suspend fun clearAll()
}