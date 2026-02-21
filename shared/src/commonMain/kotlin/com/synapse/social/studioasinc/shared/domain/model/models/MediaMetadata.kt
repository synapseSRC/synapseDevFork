package com.synapse.social.studioasinc.shared.domain.model.models

import kotlinx.datetime.Clock

data class MediaMetadata(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)
