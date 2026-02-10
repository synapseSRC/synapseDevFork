package com.synapse.social.studioasinc.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingsInsert(
    val user_id: String
)

@Serializable
data class UserPresenceInsert(
    val user_id: String
)

@Serializable
data class UserProfileInsert(
    val username: String,
    val email: String
)
