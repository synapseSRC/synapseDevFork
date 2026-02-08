package com.synapse.social.studioasinc.shared.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchUtilTest {

    @Test
    fun sanitizeSearchQuery_escapes_special_characters() {
        val input = "foo%bar_baz\\qux"
        val expected = "foo\\%bar\\_baz\\\\qux" // foo\%bar\_baz\qux (Kotlin escaped)
        val actual = sanitizeSearchQuery(input)
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeSearchQuery_limits_length() {
        val input = "a".repeat(150)
        val expected = "a".repeat(100)
        val actual = sanitizeSearchQuery(input)
        assertEquals(expected, actual)
        assertEquals(100, actual.length)
    }

    @Test
    fun sanitizeSearchQuery_trims_whitespace() {
        val input = "   hello   "
        val expected = "hello"
        val actual = sanitizeSearchQuery(input)
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeSearchQuery_handles_empty_string() {
        val input = ""
        val expected = ""
        val actual = sanitizeSearchQuery(input)
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeSearchQuery_handles_only_wildcards() {
        val input = "%%%"
        val expected = "\\%\\%\\%" // \%\%
        val actual = sanitizeSearchQuery(input)
        assertEquals(expected, actual)
    }
}
