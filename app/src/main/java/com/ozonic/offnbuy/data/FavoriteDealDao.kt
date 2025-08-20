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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteDeal)

    @Delete
    suspend fun delete(favorite: FavoriteDeal)

    @Query("SELECT * FROM favorite_deals WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavoritesForUser(userId: String): Flow<List<FavoriteDeal>>

    @Query("DELETE FROM favorite_deals WHERE userId = :userId")
    suspend fun clearFavoritesForUser(userId: String)
}