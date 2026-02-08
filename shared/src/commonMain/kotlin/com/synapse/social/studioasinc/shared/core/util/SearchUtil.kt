package com.synapse.social.studioasinc.shared.core.util

/**
 * Sanitizes a search query string for use in Supabase/Postgres ILIKE queries.
 *
 * This function:
 * 1. Trims leading and trailing whitespace.
 * 2. Limits the query length to 100 characters to prevent DoS/abuse.
 * 3. Escapes special characters used in SQL pattern matching:
 *    - Backslash (\) -> (\\)
 *    - Percent sign (%) -> (\%)
 *    - Underscore (_) -> (\_)
 *
 * This ensures that user input is treated as literal text rather than
 * wildcard patterns, preventing SQL injection-like behavior and performance issues.
 */
fun sanitizeSearchQuery(query: String): String {
    return query.trim()
        .take(100)
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
}
