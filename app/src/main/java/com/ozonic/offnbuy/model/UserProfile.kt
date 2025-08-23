package com.ozonic.offnbuy.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val uid: String = "",
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val profilePic: String? = null // Storing URL as a String
)