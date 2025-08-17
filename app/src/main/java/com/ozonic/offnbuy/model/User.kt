package com.ozonic.offnbuy.model

import android.net.Uri

data class User(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val photoUrl: Uri?,
    val isEmailVerified: Boolean
)