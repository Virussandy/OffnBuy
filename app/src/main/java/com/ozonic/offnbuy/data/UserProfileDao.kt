package com.ozonic.offnbuy.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ozonic.offnbuy.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Upsert // This conveniently handles both inserts and updates
    suspend fun upsert(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    fun getProfile(uid: String): Flow<UserProfile?>

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
    @Query("UPDATE user_profile SET uid = :toUid WHERE uid = :fromUid")
    suspend fun updateUserId(fromUid: String, toUid: String)
}