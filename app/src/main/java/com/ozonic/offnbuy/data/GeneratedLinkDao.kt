package com.ozonic.offnbuy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.model.GeneratedLink
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedLinkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(link: GeneratedLink)

    // This query now correctly filters links by the provided userId
    @Query("SELECT * FROM generated_links WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRecentLinksForUser(userId: String): Flow<List<GeneratedLink>>

    // This is a crucial function for clearing out a user's local data when they log out,
    // preventing data from one user from being shown to another.
    @Query("DELETE FROM generated_links WHERE userId = :userId")
    suspend fun clearLinksForUser(userId: String)

    // Add this new function to the interface
    @Query("SELECT * FROM generated_links WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getLinksPaginated(userId: String, limit: Int, offset: Int): List<GeneratedLink>
}