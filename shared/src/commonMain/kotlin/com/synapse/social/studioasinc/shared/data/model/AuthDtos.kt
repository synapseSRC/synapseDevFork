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
    val email: String,
    val created_at: String,
    val updated_at: String,
    val join_date: String,
    val account_premium: Boolean,
    val verify: Boolean,
    val banned: Boolean,
    val followers_count: Int,
    val following_count: Int,
    val posts_count: Int,
    val user_level_xp: Int,
    val status: String
)
