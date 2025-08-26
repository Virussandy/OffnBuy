package com.ozonic.offnbuy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.data.local.model.FavoriteDealEntity
import com.ozonic.offnbuy.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the favorite_deals table.
 */
@Dao
interface FavoriteDealDao {

    /**
     * Inserts a favorite deal into the database, replacing any existing entry with the same primary key.
     *
     * @param favorite The favorite deal to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteDealEntity)

    /**
     * Deletes a favorite deal from the database.
     *
     * @param favorite The favorite deal to delete.
     */
    @Delete
    suspend fun delete(favorite: FavoriteDealEntity)

    /**
     * Deletes all favorite deals from the database.
     */
    @Query("DELETE FROM favorite_deals")
    suspend fun clearAll()

    /**
     * Gets a flow of all favorite deals for a specific user, ordered by the date they were added.
     *
     * @param userId The ID of the user.
     * @return A [Flow] of a list of [FavoriteDealEntity] objects.
     */
    @Query("SELECT * FROM favorite_deals WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavoritesForUser(userId: String): Flow<List<FavoriteDealEntity>>

    /**
     * Deletes all favorite deals for a specific user.
     *
     * @param userId The ID of the user.
     */
    @Query("DELETE FROM favorite_deals WHERE userId = :userId")
    suspend fun clearFavoritesForUser(userId: String)

    /**
     * Updates the user ID for all favorite deals from one user to another.
     *
     * @param fromUid The original user ID.
     * @param toUid The new user ID.
     */
    @Query("UPDATE favorite_deals SET userId = :toUid WHERE userId = :fromUid")
        suspend fun updateUserId(fromUid: String, toUid: String)
}