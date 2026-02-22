package com.synapse.social.studioasinc.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
