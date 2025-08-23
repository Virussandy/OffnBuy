package com.ozonic.offnbuy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.model.FavoriteDeal
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // This handles both ADDED and MODIFIED events correctly
    suspend fun insert(favorite: FavoriteDeal)

    @Delete
    suspend fun delete(favorite: FavoriteDeal)

    // ... other methods are correct
    @Query("DELETE FROM favorite_deals")
    suspend fun clearAll()

    @Query("SELECT * FROM favorite_deals WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavoritesForUser(userId: String): Flow<List<FavoriteDeal>>

    @Query("DELETE FROM favorite_deals WHERE userId = :userId")
    suspend fun clearFavoritesForUser(userId: String)

    @Query("UPDATE favorite_deals SET userId = :toUid WHERE userId = :fromUid")
    suspend fun updateUserId(fromUid: String, toUid: String)
}