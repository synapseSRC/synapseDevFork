package com.synapse.social.studioasinc.shared.core.util

expect object NumberFormatter {
    fun format(number: Double): String
    fun formatCount(count: Int): String
}
