package com.synapse.social.studioasinc.shared.data.repository

import io.github.jan.supabase.createSupabaseClient
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchSanitizationTest {

    @Test
    fun testSanitizeSearchQuery_escapesWildcards() {
        val dummyClient = createSupabaseClient(
            supabaseUrl = "https://example.com",
            supabaseKey = "dummy"
        ) {}

        val repo = SearchRepositoryImpl(client = dummyClient)

        // Test basic characters
        assertEquals("hello", repo.sanitizeSearchQuery("hello"))

        // Test wildcards
        assertEquals("100\\%", repo.sanitizeSearchQuery("100%"))
        assertEquals("user\\_name", repo.sanitizeSearchQuery("user_name"))

        // Test backslashes
        assertEquals("C:\\\\Windows", repo.sanitizeSearchQuery("C:\\Windows"))

        // Test mixed
        assertEquals("100\\% user\\_name", repo.sanitizeSearchQuery("100% user_name"))

        // Test trimming
        assertEquals("hello", repo.sanitizeSearchQuery("  hello  "))

        // Test length limit (100)
        val longString = "a".repeat(150)
        assertEquals("a".repeat(100), repo.sanitizeSearchQuery(longString))
    }
}
