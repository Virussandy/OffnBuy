package com.ozonic.offnbuy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ozonic.offnbuy.data.local.model.UserProfileEntity
import com.ozonic.offnbuy.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the user_profile table.
 */
@Dao
interface UserProfileDao {

    /**
     * Inserts or updates a user profile in the database.
     *
     * @param profile The user profile to insert or update.
     */
    @Upsert
    suspend fun upsert(profile: UserProfileEntity)

    /**
     * Gets a flow of a user profile from the database by user ID.
     *
     * @param uid The ID of the user.
     * @return A [Flow] of a [UserProfileEntity] object, or null if not found.
     */
    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    fun getProfile(uid: String): Flow<UserProfileEntity?>

    /**
     * Deletes all user profiles from the database.
     */
    @Query("DELETE FROM user_profile")
    suspend fun clearAll()

    /**
     * Updates the user ID for a user profile from one user to another.
     *
     * @param fromUid The original user ID.
     * @param toUid The new user ID.
     */
    @Query("UPDATE user_profile SET uid = :toUid WHERE uid = :fromUid")
    suspend fun updateUserId(fromUid: String, toUid: String)
}