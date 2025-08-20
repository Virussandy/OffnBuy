package com.ozonic.offnbuy.model

import androidx.room.Entity
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Entity(tableName = "favorite_deals", primaryKeys = ["deal_id", "userId"])
data class FavoriteDeal(
    val deal_id: String = "",
    val userId: String = "",
    @ServerTimestamp
    val addedAt: Date? = null
)