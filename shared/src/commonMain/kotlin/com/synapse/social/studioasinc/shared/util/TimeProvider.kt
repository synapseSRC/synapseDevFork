package com.synapse.social.studioasinc.shared.util

import kotlinx.datetime.Instant

expect object TimeProvider {
    fun nowMillis(): Long
    fun nowInstant(): Instant
}
