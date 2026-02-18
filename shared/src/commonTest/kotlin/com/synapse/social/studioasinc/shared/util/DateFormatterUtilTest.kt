package com.synapse.social.studioasinc.shared.util

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatterUtilTest {
    @Test
    fun testAmzDateFormatter() {
        val dateTime = LocalDateTime(2023, 10, 27, 12, 34, 56)
        val formatted = DateFormatterUtil.amzDateFormatter.format(dateTime)
        assertEquals("20231027T123456Z", formatted)
    }

    @Test
    fun testAmzDateFormatter_withSingleDigitValues() {
        val dateTime = LocalDateTime(2023, 1, 2, 3, 4, 5)
        val formatted = DateFormatterUtil.amzDateFormatter.format(dateTime)
        assertEquals("20230102T030405Z", formatted)
    }
}
