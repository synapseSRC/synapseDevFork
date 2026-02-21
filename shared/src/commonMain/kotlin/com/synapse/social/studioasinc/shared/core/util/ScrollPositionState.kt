package com.synapse.social.studioasinc.shared.core.util

import kotlinx.datetime.Clock

data class ScrollPositionState(
    val position: Int = 0,
    val offset: Int = 0,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    fun isExpired(maxAgeMs: Long = 300_000): Boolean { // 5 minutes default
        return Clock.System.now().toEpochMilliseconds() - timestamp > maxAgeMs
    }
}
