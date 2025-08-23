package com.ozonic.offnbuy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.model.GeneratedLink
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Use REPLACE for both new items and updates
    suspend fun insert(link: GeneratedLink)

    @Delete // Add this missing delete method
    suspend fun delete(link: GeneratedLink)

    @Query("DELETE FROM generated_links")
    suspend fun clearAll()

    @Query("SELECT * FROM generated_links WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRecentLinksForUser(userId: String): Flow<List<GeneratedLink>>

    @Query("DELETE FROM generated_links WHERE userId = :userId")
    suspend fun clearLinksForUser(userId: String)

    @Query("SELECT * FROM generated_links WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getLinksPaginated(userId: String, limit: Int, offset: Int): List<GeneratedLink>

    @Query("UPDATE generated_links SET userId = :toUid WHERE userId = :fromUid")
    suspend fun updateUserId(fromUid: String, toUid: String)
}