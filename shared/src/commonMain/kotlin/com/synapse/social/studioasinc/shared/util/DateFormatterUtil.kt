package com.synapse.social.studioasinc.shared.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

internal object DateFormatterUtil {
    val amzDateFormatter = LocalDateTime.Format {
        year()
        monthNumber()
        day()
        char('T')
        hour()
        minute()
        second()
        char('Z')
    }
}
