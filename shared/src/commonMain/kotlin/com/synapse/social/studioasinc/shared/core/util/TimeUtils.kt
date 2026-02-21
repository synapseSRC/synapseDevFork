package com.synapse.social.studioasinc.shared.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

object TimeUtils {
    fun getTimeAgo(timestamp: Long): String {
        val now = Clock.System.now().toEpochMilliseconds()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "now"
            diff < 3600_000 -> "${diff / 60_000}m"
            diff < 86400_000 -> "${diff / 3600_000}h"
            diff < 604800_000 -> "${diff / 86400_000}d"
            else -> "${diff / 604800_000}w"
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        
        return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    }

    fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
    
    fun getCurrentIsoTime(): String = Clock.System.now().toString()
}
