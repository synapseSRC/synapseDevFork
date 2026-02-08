package com.synapse.social.studioasinc.shared.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchSanitizationTest {

    @Test
    fun testSanitizeNormal() {
        val input = "hello"
        val expected = "hello"
        assertEquals(expected, input.sanitizeForSearch())
    }

    @Test
    fun testSanitizeTrim() {
        val input = "  hello  "
        val expected = "hello"
        assertEquals(expected, input.sanitizeForSearch())
    }

    @Test
    fun testSanitizePercent() {
        val input = "100%"
        val expected = "100\\%"
        assertEquals(expected, input.sanitizeForSearch())
    }

    @Test
    fun testSanitizeUnderscore() {
        val input = "user_name"
        val expected = "user\\_name"
        assertEquals(expected, input.sanitizeForSearch())
    }

    @Test
    fun testSanitizeMixed() {
        val input = "  user_%  "
        val expected = "user\\_\\%"
        assertEquals(expected, input.sanitizeForSearch())
    }
}
