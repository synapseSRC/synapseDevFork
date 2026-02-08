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
        // "%" -> "\\%"
        assertEquals("\\%", sanitizeSearchQuery("%"))
        // "_" -> "\\_"
        assertEquals("\\_", sanitizeSearchQuery("_"))

        // "test%" -> "test\\%"
        assertEquals("test\\%", sanitizeSearchQuery("test%"))
        // "test_" -> "test\\_"
        assertEquals("test\\_", sanitizeSearchQuery("test_"))
    }

    @Test
    fun testSanitizeSearchQuery_escape() {
        // "\\" -> "\\\\"
        assertEquals("\\\\", sanitizeSearchQuery("\\"))

        // "test\\" -> "test\\\\"
        assertEquals("test\\\\", sanitizeSearchQuery("test\\"))
    }

    @Test
    fun testSanitizeSearchQuery_mixed() {
        // Input: "test\foo%bar_baz"
        // Expected: "test\\foo\%bar\_baz"
        assertEquals("test\\\\foo\\%bar\\_baz", sanitizeSearchQuery("test\\foo%bar_baz"))
    }

    @Test
    fun testSanitizeSearchQuery_limit() {
        val longString = "a".repeat(150)
        val sanitized = sanitizeSearchQuery(longString)
        assertEquals(100, sanitized.length)
        // If we truncate before escaping, we get 100 'a's.
        // If we escape first, then truncate...
        // My implementation: trim().take(100).replace(...)
        // So truncation happens first.
        assertEquals("a".repeat(100), sanitized)
    }
}
