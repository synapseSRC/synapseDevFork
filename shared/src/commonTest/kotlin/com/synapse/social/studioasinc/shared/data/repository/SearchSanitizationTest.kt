package com.synapse.social.studioasinc.shared.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchSanitizationTest {

    @Test
    fun testEmptyQuery() {
        assertEquals("", "".sanitizeForSearch())
        assertEquals("", "   ".sanitizeForSearch())
    }

    @Test
    fun testNormalQuery() {
        assertEquals("hello", "hello".sanitizeForSearch())
        assertEquals("hello world", "  hello world  ".sanitizeForSearch())
    }

    @Test
    fun testSpecialCharacters() {
        // % -> \%
        // Kotlin literal "100\\%" is a string containing: 1, 0, 0, \, %
        assertEquals("100\\%", "100%".sanitizeForSearch())

        // _ -> \_
        assertEquals("hello\\_world", "hello_world".sanitizeForSearch())

        // \ -> \\
        assertEquals("C:\\\\Path", "C:\\Path".sanitizeForSearch())
    }

    @Test
    fun testTruncation() {
        val longString = "a".repeat(200)
        assertEquals(100, longString.sanitizeForSearch().length)
        val expected = "a".repeat(100)
        assertEquals(expected, longString.sanitizeForSearch())
    }
}
