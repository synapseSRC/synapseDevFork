package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
