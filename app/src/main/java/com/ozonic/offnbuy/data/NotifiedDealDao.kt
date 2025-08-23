package com.ozonic.offnbuy.data


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ozonic.offnbuy.model.NotifiedDeal
import kotlinx.coroutines.flow.Flow

@Dao
interface NotifiedDealDao {
    @Upsert // Handles both inserts and updates efficiently
    suspend fun upsert(notifiedDeal: NotifiedDeal)

    @Upsert
    suspend fun upsertAll(deals: List<NotifiedDeal>)

    @Query("SELECT * FROM notified_deals ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotifiedDeal>>

    @Query("DELETE FROM notified_deals WHERE deal_id = :dealId")
    suspend fun delete(dealId: String)

    @Query("DELETE FROM notified_deals")
    suspend fun clearAll()
}