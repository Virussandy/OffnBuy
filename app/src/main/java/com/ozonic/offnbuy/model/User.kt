package com.ozonic.offnbuy.model

data class User(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean
)