package com.ozonic.offnbuy.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Entity(tableName = "generated_links", primaryKeys = ["url", "userId"])

data class GeneratedLink(
    val url: String = "",
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)