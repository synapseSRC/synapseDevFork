package com.synapse.social.studioasinc.shared.core.util

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual object NumberFormatter {
    actual fun format(number: Double): String {
        if (number < 10000) {
            return number.toLong().toString()
        }

        val formatter = NSNumberFormatter().apply {
            minimumFractionDigits = 0u
            maximumFractionDigits = 1u
        }

        return when {
            number < 1_000_000 -> (formatter.stringFromNumber(NSNumber(number / 1_000)) ?: "0") + "K"
            number < 1_000_000_000 -> (formatter.stringFromNumber(NSNumber(number / 1_000_000)) ?: "0") + "M"
            number < 1_000_000_000_000L -> (formatter.stringFromNumber(NSNumber(number / 1_000_000_000)) ?: "0") + "B"
            else -> (formatter.stringFromNumber(NSNumber(number / 1_000_000_000_000L)) ?: "0") + "T"
        }
    }

    actual fun formatCount(count: Int): String {
        val formatter = NSNumberFormatter().apply {
            minimumFractionDigits = 0u
            maximumFractionDigits = 1u
        }

        return when {
            count >= 1_000_000_000 -> {
                val formatted = count / 1_000_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}B"
                } else {
                    (formatter.stringFromNumber(NSNumber(formatted)) ?: "0") + "B"
                }
            }
            count >= 1_000_000 -> {
                val formatted = count / 1_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}M"
                } else {
                    (formatter.stringFromNumber(NSNumber(formatted)) ?: "0") + "M"
                }
            }
            count >= 1_000 -> {
                val formatted = count / 1_000.0
                (formatter.stringFromNumber(NSNumber(formatted)) ?: "0") + "K"
            }
            else -> count.toString()
        }
    }
}
