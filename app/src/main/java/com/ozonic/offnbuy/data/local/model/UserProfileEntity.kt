package com.ozonic.offnbuy.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ozonic.offnbuy.domain.model.UserProfile

/**
 * Represents a user profile in the local database.
 *
 * @property uid The unique identifier for the user.
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property phone The phone number of the user.
 * @property profilePic The URL of the user's profile picture.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String = "", // Added default value
    val name: String? = null, // Added default value
    val email: String? = null, // Added default value
    val phone: String? = null, // Added default value
    val profilePic: String? = null // Added default value
) {
    /**
     * Converts this entity to a [UserProfile] domain model.
     *
     * @return The [UserProfile] domain model.
     */
    fun toDomainModel(): UserProfile {
        return UserProfile(
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            profilePic = profilePic
        )
    }
}