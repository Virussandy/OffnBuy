package com.ozonic.offnbuy.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "generated_links",indices = [Index(value = ["url"], unique = true)])

data class GeneratedLink(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String
)