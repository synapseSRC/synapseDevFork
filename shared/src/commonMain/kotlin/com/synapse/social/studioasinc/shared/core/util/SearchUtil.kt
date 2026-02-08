package com.synapse.social.studioasinc.shared.core.util

/**
 * Sanitizes search query string to prevent wildcard abuse and optimize performance.
 *
 * @param query The user's input string.
 * @return The sanitized string safe for use in `ilike` queries.
 */
fun sanitizeSearchQuery(query: String): String {
    // 1. Trim whitespace
    // 2. Limit length to 100 characters to prevent massive regex processing
    val trimmed = query.trim().take(100)

    // 3. Escape special characters for SQL `ilike`
    // Note: Order matters. Escape backslash first.
    return trimmed
        .replace("\\", "\\\\") // Escape backslash -> \\ (escaped in Kotlin string as well)
        .replace("%", "\\%")   // Escape percent -> \%
        .replace("_", "\\_")   // Escape underscore -> \_
}
