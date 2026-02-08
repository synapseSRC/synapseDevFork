package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.util.sanitizeSearchQuery
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchSanitizationTest {

    @Test
    fun testSanitizeSearchQuery_escapesWildcards() {

        assertEquals("hello", sanitizeSearchQuery("hello"))


        assertEquals("100\\%", sanitizeSearchQuery("100%"))
        assertEquals("user\\_name", sanitizeSearchQuery("user_name"))


        assertEquals("C:\\\\Windows", sanitizeSearchQuery("C:\\Windows"))


        assertEquals("100\\% user\\_name", sanitizeSearchQuery("100% user_name"))


        assertEquals("hello", sanitizeSearchQuery("  hello  "))


        val longString = "a".repeat(150)
        assertEquals("a".repeat(100), sanitizeSearchQuery(longString))
    }
}
