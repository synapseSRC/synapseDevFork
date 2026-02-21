package com.synapse.social.studioasinc.shared.core.util





object NumberFormatter {



    fun format(number: Double): String {
        if (number < 10000) {
            return number.toLong().toString()
        }

        val formatted = (number / unit * 10).toInt() / 10.0
        return when {
            number < 1_000_000 -> ((number / 1000 * 10).toInt() / 10.0).toString() + "K"
            number < 1_000_000_000 -> ((number / 1000000 * 10).toInt() / 10.0).toString() + "M"
            number < 1_000_000_000_000L -> ((number / 1000000000 * 10).toInt() / 10.0).toString() + "B"
            else -> ((number / 1000000000000.0 * 10).toInt() / 10.0).toString() + "T"
        }
    }



    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000_000 -> {
                val formatted = count / 1_000_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}B"
                } else {
                    "${((formatted * 10).toInt() / 10.0)}B"
                }
            }
            count >= 1_000_000 -> {
                val formatted = count / 1_000_000.0
                if (formatted == formatted.toLong().toDouble()) {
                    "${formatted.toLong()}M"
                } else {
                    "${((formatted * 10).toInt() / 10.0)}M"
                }
            }
            count >= 1_000 -> {
                val formatted = count / 1_000.0
                "${((formatted * 10).toInt() / 10.0)}K"
            }
            else -> count.toString()
        }
    }
}
