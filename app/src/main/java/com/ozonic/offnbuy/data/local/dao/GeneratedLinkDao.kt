package com.ozonic.offnbuy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozonic.offnbuy.data.local.model.GeneratedLinkEntity
import com.ozonic.offnbuy.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the generated_links table.
 */
@Dao
interface GeneratedLinkDao {

    /**
     * Inserts a generated link into the database, replacing any existing entry with the same primary key.
     *
     * @param link The generated link to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: GeneratedLinkEntity)

    /**
     * Deletes a generated link from the database.
     *
     * @param link The generated link to delete.
     */
    @Delete
    suspend fun delete(link: GeneratedLinkEntity)

    /**
     * Deletes all generated links from the database.
     */
    @Query("DELETE FROM generated_links")
    suspend fun clearAll()

    /**
     * Gets a flow of all generated links for a specific user, ordered by the date they were created.
     *
     * @param userId The ID of the user.
     * @return A [Flow] of a list of [GeneratedLinkEntity] objects.
     */
    @Query("SELECT * FROM generated_links WHERE userId = :userId ORDER BY createdAt ASC")
    fun getRecentLinksForUser(userId: String): Flow<List<GeneratedLinkEntity>>

    /**
     * Deletes all generated links for a specific user.
     *
     * @param userId The ID of the user.
     */
    @Query("DELETE FROM generated_links WHERE userId = :userId")
    suspend fun clearLinksForUser(userId: String)

    /**
     * Gets a paginated list of generated links for a specific user.
     *
     * @param userId The ID of the user.
     * @param limit The number of items to retrieve.
     * @param offset The starting position.
     * @return A list of [GeneratedLinkEntity] objects.
     */
    @Query("SELECT * FROM generated_links WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getLinksPaginated(userId: String, limit: Int, offset: Int): List<GeneratedLinkEntity>

    /**
     * Updates the user ID for all generated links from one user to another.
     *
     * @param fromUid The original user ID.
     * @param toUid The new user ID.
     */
    @Query("UPDATE generated_links SET userId = :toUid WHERE userId = :fromUid")
    suspend fun updateUserId(fromUid: String, toUid: String)
}