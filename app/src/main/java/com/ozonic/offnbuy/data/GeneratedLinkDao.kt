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

    @Query("SELECT * FROM generated_links ORDER BY id DESC")
    fun getRecentLinks(): Flow<List<GeneratedLink>>
}