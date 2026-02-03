package com.synapse.social.studioasinc.core.media

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for media operations.
 */
data class MediaConfig(
    val maxImageSize: Long = 10 * 1024 * 1024, // 10MB
    val maxVideoDuration: Duration = 60.seconds,
    val compressImages: Boolean = true,
    val thumbnailQuality: Int = 80
)
