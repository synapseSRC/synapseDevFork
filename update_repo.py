import re
import os

file_path = "app/src/main/java/com/synapse/social/studioasinc/data/repository/PostRepository.kt"

if not os.path.exists(file_path):
    print(f"File not found: {file_path}")
    exit(1)

with open(file_path, "r") as f:
    content = f.read()

# 1. Add imports
imports = """import android.content.SharedPreferences
import kotlinx.datetime.Instant
"""
if "import android.content.SharedPreferences" not in content:
    content = content.replace("package com.synapse.social.studioasinc.data.repository", "package com.synapse.social.studioasinc.data.repository\n\n" + imports)

# 2. Modify Constructor
# Regex to match the constructor with flexibility on whitespace
constructor_pattern = r"class PostRepository @Inject constructor\(\s*private val postDao: PostDao,\s*private val client: JanSupabaseClient\s*\)"
constructor_replacement = "class PostRepository @Inject constructor(\n    private val postDao: PostDao,\n    private val client: JanSupabaseClient,\n    private val prefs: SharedPreferences\n)"

content = re.sub(constructor_pattern, constructor_replacement, content)

# 3. Add Companion Object Constant
if "companion object" not in content:
    # Add it inside the class
    content = content.replace(") {", ") {\n\n    companion object {\n        private const val TAG = \"PostRepository\"\n        private const val PREF_LAST_SYNC_TIME = \"last_post_sync_time\"\n    }", 1)
    # Remove existing TAG definitions if strictly equal to patterns
    content = re.sub(r'^\s*private const val TAG = "PostRepository"', "", content, flags=re.MULTILINE)
else:
    if "PREF_LAST_SYNC_TIME" not in content:
        content = content.replace("companion object {", "companion object {\n        private const val PREF_LAST_SYNC_TIME = \"last_post_sync_time\"")

# 4. Replace syncDeletedPosts
start_marker = "private suspend fun syncDeletedPosts() {"
end_marker = "suspend fun getUserPosts(userId: String): Result<List<Post>>"

new_method = """    @androidx.annotation.VisibleForTesting
    internal suspend fun syncDeletedPosts() {
        try {
            val localIds = postDao.getAllPostIds()
            if (localIds.isEmpty()) return

            val lastSync = prefs.getLong(PREF_LAST_SYNC_TIME, 0L)

            if (lastSync == 0L) {
                 android.util.Log.d(TAG, "First sync or migration: Checking all local posts for deletion (O(N))")
                 val idsToDelete = mutableListOf<String>()

                 // Process in chunks of 50 to respect URL length limits
                 localIds.chunked(50).forEach { chunk ->
                     try {
                         val response = client.from("posts")
                             .select(columns = Columns.raw("id, is_deleted")) {
                                 filter { isIn("id", chunk) }
                             }
                             .decodeList<JsonObject>()

                         idsToDelete.addAll(findDeletedIds(chunk, response))

                     } catch (e: Exception) {
                         android.util.Log.e(TAG, "Failed to check chunk existence", e)
                     }
                 }

                 if (idsToDelete.isNotEmpty()) {
                     val uniqueIdsToDelete = idsToDelete.distinct()
                     android.util.Log.d(TAG, "Syncing deletions: removing ${uniqueIdsToDelete.size} posts")
                     uniqueIdsToDelete.chunked(500).forEach { batch ->
                         postDao.deletePosts(batch)
                     }
                 }
            } else {
                 val isoTime = Instant.fromEpochMilliseconds(lastSync).toString()
                 android.util.Log.d(TAG, "Syncing deleted posts since $isoTime")

                 val response = client.from("posts")
                     .select(columns = Columns.raw("id")) {
                         filter {
                             gt("updated_at", isoTime)
                             eq("is_deleted", true)
                         }
                     }
                     .decodeList<JsonObject>()

                 val deletedIds = response.mapNotNull { it[\"id\"]?.jsonPrimitive?.contentOrNull }

                 if (deletedIds.isNotEmpty()) {
                      android.util.Log.d(TAG, "Found ${deletedIds.size} deleted posts via timestamp sync")
                      postDao.deletePosts(deletedIds)
                 }
            }

            prefs.edit().putLong(PREF_LAST_SYNC_TIME, System.currentTimeMillis()).apply()

        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to sync deleted posts", e)
        }
    }

    """

# Robust replacement finding
start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + new_method + content[end_idx:]
    with open(file_path, "w") as f:
        f.write(content)
    print("Successfully updated PostRepository.kt")
else:
    print(f"Could not find method boundaries. Start: {start_idx}, End: {end_idx}")
    # Fallback debug
    if start_idx == -1:
        print("Could not find start marker")
    if end_idx == -1:
        print("Could not find end marker")
    exit(1)
