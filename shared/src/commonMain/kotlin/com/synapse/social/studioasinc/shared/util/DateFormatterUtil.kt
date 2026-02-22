package com.synapse.social.studioasinc.shared.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

internal object DateFormatterUtil {
    val amzDateFormatter = LocalDateTime.Format {
        year()
        monthNumber(Padding.ZERO)
        day(Padding.ZERO)
        char('T')
        hour(Padding.ZERO)
        minute(Padding.ZERO)
        second(Padding.ZERO)
        char('Z')
    }
}
