package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkedAccount(
    val id: String,
    val platform: String,
    val username: String,
    val url: String,
    @SerialName("is_verified")
    val isVerified: Boolean = false
)
