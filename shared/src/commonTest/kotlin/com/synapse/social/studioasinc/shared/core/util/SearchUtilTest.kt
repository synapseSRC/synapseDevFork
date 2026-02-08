package com.synapse.social.studioasinc.shared.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchUtilTest {

    @Test
    fun testSanitizeSearchQuery_basic() {
        assertEquals("test", sanitizeSearchQuery("test"))
        assertEquals("test", sanitizeSearchQuery("  test  "))
    }

    @Test
    fun testSanitizeSearchQuery_wildcards() {

        assertEquals("\\%", sanitizeSearchQuery("%"))

        assertEquals("\\_", sanitizeSearchQuery("_"))


        assertEquals("test\\%", sanitizeSearchQuery("test%"))

        assertEquals("test\\_", sanitizeSearchQuery("test_"))
    }

    @Test
    fun testSanitizeSearchQuery_escape() {

        assertEquals("\\\\", sanitizeSearchQuery("\\"))


        assertEquals("test\\\\", sanitizeSearchQuery("test\\"))
    }

    @Test
    fun testSanitizeSearchQuery_mixed() {


        assertEquals("test\\\\foo\\%bar\\_baz", sanitizeSearchQuery("test\\foo%bar_baz"))
    }

    @Test
    fun testSanitizeSearchQuery_limit() {
        val longString = "a".repeat(150)
        val sanitized = sanitizeSearchQuery(longString)
        assertEquals(100, sanitized.length)




        assertEquals("a".repeat(100), sanitized)
    }
}
