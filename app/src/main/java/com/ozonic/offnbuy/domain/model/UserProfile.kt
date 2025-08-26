package com.ozonic.offnbuy.domain.model

/**
 * Represents a user profile in the domain layer.
 *
 * @property uid The unique identifier for the user.
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property phone The phone number of the user.
 * @property profilePic The URL of the user's profile picture.
 */
data class UserProfile(
    val uid: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val profilePic: String?
)