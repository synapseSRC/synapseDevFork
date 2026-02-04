package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Like model for Supabase database
 */
@Serializable
data class Like(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("target_id")
    val targetId: String,
    @SerialName("target_type")
    val targetType: String = "post",
    @SerialName("created_at")
    val createdAt: String? = null
)
