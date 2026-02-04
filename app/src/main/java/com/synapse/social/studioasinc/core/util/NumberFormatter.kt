package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.core.util

import java.text.DecimalFormat

/**
 * A utility object for formatting numbers.
 */
object NumberFormatter {

    /**
     * Formats a number into a shorthand form (e.g., 1K, 1M, 1B, 1T).
     *
     * @param number The number to format.
     * @return The formatted string.
     */
    fun format(number: Double): String {
        if (number < 10000) {
            return number.toLong().toString()
        }

        val decimalFormat = DecimalFormat("0.0")
        return when {
            number < 1_000_000 -> decimalFormat.format(number / 1_000) + "K"
            number < 1_000_000_000 -> decimalFormat.format(number / 1_000_000) + "M"
            number < 1_000_000_000_000L -> decimalFormat.format(number / 1_000_000_000) + "B"
            else -> decimalFormat.format(number / 1_000_000_000_000L) + "T"
        }
    }

    /**
     * Format large numbers for display (e.g., 1.2K, 3.5M, 1.0B)
     */
    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000_000 -> {
                val formatted = count / 1_000_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}B"
                } else {
                    String.format("%.1fB", formatted)
                }
            }
            count >= 1_000_000 -> {
                val formatted = count / 1_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}M"
                } else {
                    String.format("%.1fM", formatted)
                }
            }
            count >= 1_000 -> {
                val formatted = count / 1_000.0
                String.format("%.1fK", formatted)
            }
            else -> count.toString()
        }
    }
}
