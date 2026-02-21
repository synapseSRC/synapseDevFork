package com.synapse.social.studioasinc.shared.core.util

import kotlin.math.roundToInt

object NumberFormatter {

    fun format(number: Double): String {
        if (number < 10000) {
            return number.toLong().toString()
        }

        return when {
            number < 1_000_000 -> formatDecimal(number / 1_000) + "K"
            number < 1_000_000_000 -> formatDecimal(number / 1_000_000) + "M"
            number < 1_000_000_000_000L -> formatDecimal(number / 1_000_000_000) + "B"
            else -> formatDecimal(number / 1_000_000_000_000L) + "T"
        }
    }

    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000_000 -> {
                val formatted = count / 1_000_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}B"
                } else {
                    formatDecimal(formatted) + "B"
                }
            }
            count >= 1_000_000 -> {
                val formatted = count / 1_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}M"
                } else {
                    formatDecimal(formatted) + "M"
                }
            }
            count >= 1_000 -> {
                val formatted = count / 1_000.0
                formatDecimal(formatted) + "K"
            }
            else -> count.toString()
        }
    }

    private fun formatDecimal(number: Double): String {
        // Round to 1 decimal place
        val rounded = (number * 10).roundToInt() / 10.0
        return if (rounded % 1.0 == 0.0) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }
}
