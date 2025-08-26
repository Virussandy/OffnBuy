package com.ozonic.offnbuy.domain.model

import android.net.Uri

/**
 * Represents a user in the domain layer.
 *
 * @property uid The unique identifier for the user.
 * @property displayName The display name of the user.
 * @property email The email address of the user.
 * @property phoneNumber The phone number of the user.
 * @property photoUrl The URL of the user's profile picture.
 * @property isEmailVerified Whether the user's email address has been verified.
 * @property isAnonymous Whether the user is anonymous.
 */
data class User(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val photoUrl: Uri?,
    val isEmailVerified: Boolean,
    val isAnonymous: Boolean
)