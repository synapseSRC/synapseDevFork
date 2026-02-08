package com.synapse.social.studioasinc.shared.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchSanitizationTest {

    @Test
    fun testSanitizeSearchQuery() {
        // Standard
        assertEquals("john", sanitizeSearchQuery("john"))

        // Trimming
        assertEquals("john", sanitizeSearchQuery("  john  "))

        // Special chars escaping
        assertEquals("100\\%", sanitizeSearchQuery("100%"))
        assertEquals("user\\_name", sanitizeSearchQuery("user_name"))

        // Length limit
        val longString = "a".repeat(150)
        val expected = "a".repeat(100)
        assertEquals(expected, sanitizeSearchQuery(longString))
    }
}
