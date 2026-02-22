package com.synapse.social.studioasinc.shared.domain.model.models



data class MediaMetadata(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
