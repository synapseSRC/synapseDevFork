package com.synapse.social.studioasinc.shared.data.local.database





data class UserEntity(

    val uid: String,
    val username: String?,
    val email: String?,
    val avatarUrl: String?,
    val isVerified: Boolean
)
