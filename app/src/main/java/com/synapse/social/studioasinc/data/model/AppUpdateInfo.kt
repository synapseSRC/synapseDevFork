package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing app update information fetched from the backend.
 */
@Serializable
data class AppUpdateInfo(
    @SerialName("version_code")
    val versionCode: Long,

    @SerialName("version_name")
    val versionName: String,

    @SerialName("release_notes")
    val releaseNotes: String? = null,

    @SerialName("mandatory")
    val isMandatory: Boolean = false,

    @SerialName("download_url")
    val downloadUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
