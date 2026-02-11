package com.synapse.social.studioasinc.shared.util

import kotlinx.datetime.Instant

actual object TimeProvider {
    actual fun nowMillis(): Long = java.lang.System.currentTimeMillis()
    actual fun nowInstant(): Instant = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
}
