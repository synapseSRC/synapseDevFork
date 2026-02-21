package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.ReactionType
import com.synapse.social.studioasinc.shared.domain.model.UserReaction
import com.synapse.social.studioasinc.shared.data.paging.PostPagingSource
import com.synapse.social.studioasinc.shared.core.util.MentionParser
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import io.github.aakira.napier.Napier
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.datetime.Clock

class PostRepository(
    private val storageDatabase: StorageDatabase,
    private val client: io.github.jan.supabase.SupabaseClient
) {

    fun getPostsPaged(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(client.from("posts")) }
        ).flow
    }

    // Simple map for now, assuming single thread access or relying on map internals if KMP compliant
    // In KMP, MutableMap is not thread safe. But for now I'll use it directly to match original logic structure.
    private val profileCache = mutableMapOf<String, CacheEntry<ProfileData>>()
    private val postsCache = mutableMapOf<String, CacheEntry<Post>>()

    private data class CacheEntry<T>(val data: T, val timestamp: Long = Clock.System.now().toEpochMilliseconds()) {
        fun isExpired() = Clock.System.now().toEpochMilliseconds() - timestamp > CACHE_EXPIRATION_MS
    }

    companion object {
        private const val CACHE_EXPIRATION_MS = 5 * 60 * 1000L
        private const val TAG = "PostRepository"
        private val PGRST_REGEX = Regex("PGRST\\d+")
        private val COLUMN_REGEX = Regex("column \"([^\"]+)\"")

        internal fun findDeletedIds(localChunk: List<String>, serverResponse: List<JsonObject>): List<String> {
            val serverIds = serverResponse.mapNotNull { it["id"]?.jsonPrimitive?.contentOrNull }.toSet()
            val missingIds = localChunk.filter { !serverIds.contains(it) }
            val softDeletedIds = serverResponse.filter {
                it["is_deleted"]?.jsonPrimitive?.booleanOrNull == true
            }.mapNotNull { it["id"]?.jsonPrimitive?.contentOrNull }

            return (missingIds + softDeletedIds).distinct()
        }
    }

    private data class ProfileData(
        val username: String?,
        val avatarUrl: String?,
        val isVerified: Boolean
    )

    fun constructMediaUrl(storagePath: String): String {
        return SupabaseClient.constructMediaUrl(storagePath)
    }

    private fun constructAvatarUrl(storagePath: String): String {
        return SupabaseClient.constructAvatarUrl(storagePath)
    }

    private fun mapSupabaseError(exception: Exception): String {
        val message = exception.message ?: "Unknown error"
        val pgrstMatch = PGRST_REGEX.find(message)
        if (pgrstMatch != null) {
            Napier.e("Supabase PostgREST error code: ${pgrstMatch.value}", tag = TAG)
        }
        Napier.e("Supabase error: $message", exception, tag = TAG)


        return when {
            message.contains("PGRST200") -> "Relation/table not found in schema"
            message.contains("PGRST100") -> "Database column mismatch: ${extractColumnInfo(message)}"
            message.contains("PGRST116") -> "No rows returned (expected single)"
            message.contains("relation", ignoreCase = true) -> "Database table does not exist"
            message.contains("column", ignoreCase = true) -> "Database column mismatch: ${extractColumnInfo(message)}"
            message.contains("does not exist", ignoreCase = true) -> "Database column mismatch: ${extractColumnInfo(message)}"
            message.contains("policy", ignoreCase = true) || message.contains("rls", ignoreCase = true) ->
                "Permission denied. Row-level security policy blocked this operation."
            message.contains("connection", ignoreCase = true) || message.contains("network", ignoreCase = true) ->
                "Connection failed. Please check your internet connection."
            message.contains("timeout", ignoreCase = true) -> "Request timed out. Please try again."
            message.contains("unauthorized", ignoreCase = true) -> "Permission denied."
            message.contains("serialization", ignoreCase = true) -> "Data format error."
            else -> "Database error: $message"
        }
    }

    private fun extractColumnInfo(message: String): String {
        val columnMatch = COLUMN_REGEX.find(message)
        return columnMatch?.groupValues?.get(1) ?: "unknown column"
    }

    private fun invalidateCache() {
        postsCache.clear()
    }

    suspend fun createPost(post: Post): Result<Post> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                return@withContext Result.failure(Exception("Supabase not configured."))
            }

            if (post.username == null) {
                val profile = fetchUserProfile(post.authorUid)
                if (profile != null) {
                    post.username = profile.username
                    post.avatarUrl = profile.avatarUrl
                    post.isVerified = profile.isVerified
                }
            }

            val postDto = post.toInsertDto()

            Napier.d("Creating post with DTO fields: ${getFieldNames(postDto)}", tag = TAG)
            Napier.d("Post author_uid: ${postDto.authorUid}", tag = TAG)
            Napier.d("Current auth user: ${client.auth.currentUserOrNull()?.id}", tag = TAG)

            client.from("posts").insert(postDto)
            storageDatabase.postQueries.insertPost(PostMapper.toEntity(post))
            invalidateCache()

            processMentions(post.id, post.postText ?: "", post.authorUid)

            Napier.d("Post created successfully: ${post.id}", tag = TAG)
            Result.success(post)
        } catch (e: Exception) {
            Napier.e("Failed to create post", e, tag = TAG)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    private fun getFieldNames(dto: PostInsertDto): String {
        return "id, key, author_uid, post_text, post_image, post_type, post_visibility, " +
               "post_hide_views_count, post_hide_like_count, post_hide_comments_count, " +
               "post_disable_comments, publish_date, timestamp, likes_count, comments_count, " +
               "views_count, reshares_count, media_items, has_poll, poll_question, poll_options, " +
               "poll_end_time, poll_allow_multiple, has_location, location_name, location_address, " +
               "location_latitude, location_longitude, location_place_id, youtube_url"
    }

    suspend fun getPost(postId: String): Result<Post?> = withContext(Dispatchers.IO) {
        try {
            val post = storageDatabase.postQueries.selectById(postId).executeAsOneOrNull()?.let { entity ->
                val model = PostMapper.toModel(entity)

                if (model.username == null) {
                    fetchUserProfile(model.authorUid)?.let { profile ->
                        model.username = profile.username
                        model.avatarUrl = profile.avatarUrl
                        model.isVerified = profile.isVerified
                    }
                }
                model
            }
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(Exception("Error getting post from database: ${e.message}"))
        }
    }

    fun getPosts(): Flow<Result<List<Post>>> {
        return flow {
             val posts = storageDatabase.postQueries.selectAll().executeAsList().map { PostMapper.toModel(it) }
             emit(Result.success(posts))
        }.flatMapLatest { result ->
            flow {
                val posts = result.getOrNull() ?: emptyList()

                posts.forEach { post ->
                    if (post.username == null) {
                        profileCache[post.authorUid]?.data?.let { profile ->
                            post.username = profile.username
                            post.avatarUrl = profile.avatarUrl
                            post.isVerified = profile.isVerified
                        }
                    }
                }

                emit(Result.success(posts))

                val missingUserIds = posts.filter { it.username == null }
                    .map { it.authorUid }
                    .distinct()
                    .filter { userId ->
                        profileCache[userId]?.let { !it.isExpired() } != true
                    }


                if (missingUserIds.isNotEmpty()) {
                    fetchUserProfilesBatch(missingUserIds)
                    posts.forEach { post ->
                        if (post.username == null) {
                            profileCache[post.authorUid]?.data?.let { profile ->
                                post.username = profile.username
                                post.avatarUrl = profile.avatarUrl
                                post.isVerified = profile.isVerified
                            }
                        }
                    }
                    emit(Result.success(posts.toList()))
                }
            }
        }.catch { e ->
            emit(Result.failure(Exception("Error getting posts from database: ${e.message}")))
        }
    }

    fun getReelsPaged(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(client.from("posts")) }
        ).flow
    }

    suspend fun refreshPosts(page: Int, pageSize: Int): Result<Unit> {
        return try {
            val offset = page * pageSize

            val response = client.from("posts")
                .select(
                    columns = Columns.raw("""
                        *,
                        users!posts_author_uid_fkey(uid, username, avatar, verify),
                        latest_comments:comments(id, content, user_id, created_at, users(username))
                    """.trimIndent())
                ) {
                    range(offset.toLong(), (offset + pageSize - 1).toLong())
                }
                .decodeList<PostSelectDto>()

            val posts = response.map { postDto ->
                postDto.toDomain(::constructMediaUrl, ::constructAvatarUrl).also { post ->

                    postDto.user?.let { user ->
                        if (user.uid.isNotEmpty()) {
                            profileCache[user.uid] = CacheEntry(
                                ProfileData(
                                    username = user.username,
                                    avatarUrl = user.avatarUrl?.let { constructAvatarUrl(it) },
                                    isVerified = user.isVerified ?: false
                                )
                            )
                        }
                    }
                }
            }

            val postsWithReactions = populatePostReactions(posts)
            val postsWithPolls = populatePostPolls(postsWithReactions)

            storageDatabase.transaction {
                postsWithPolls.forEach { post ->
                     storageDatabase.postQueries.insertPost(PostMapper.toEntity(post))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to fetch user posts: ${e.message}", e, tag = TAG)
            Result.failure(e)
        }
    }
    private suspend fun syncDeletedPosts() {
        try {
            val localIds = storageDatabase.postQueries.selectIds().executeAsList()
            if (localIds.isEmpty()) return
            val idsToDelete = mutableListOf<String>()
            localIds.chunked(50).forEach { chunk ->
                try {
                    val response = client.from("posts")
                        .select(columns = Columns.raw("id, is_deleted")) {
                            filter { isIn("id", chunk) }
                        }
                        .decodeList<JsonObject>()
                    idsToDelete.addAll(findDeletedIds(chunk, response))
                } catch (e: Exception) {
                    Napier.e("Failed to check chunk existence", e, tag = TAG)
                }
            }
            if (idsToDelete.isNotEmpty()) {
                val uniqueIdsToDelete = idsToDelete.distinct()
                Napier.d("Syncing deletions: removing ${uniqueIdsToDelete.size} posts", tag = TAG)
                storageDatabase.transaction {
                    uniqueIdsToDelete.forEach { id ->
                        storageDatabase.postQueries.deleteById(id)
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e("Failed to sync deleted posts", e, tag = TAG)
        }
    }
    suspend fun getUserPosts(userId: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val response = client.from("posts")
                .select(
                    columns = Columns.raw("""
                        *,
                        users!posts_author_uid_fkey(uid, username, avatar, verify),
                        latest_comments:comments(id, content, user_id, created_at, users(username))
                    """.trimIndent())
                ) {
                    filter { eq("author_uid", userId) }
                    order("timestamp", order = Order.DESCENDING)
                }
                .decodeList<PostSelectDto>()
            val posts = response.map { postDto ->
                postDto.toDomain(::constructMediaUrl, ::constructAvatarUrl).also { post ->
                     postDto.user?.let { user ->
                        if (user.uid.isNotEmpty()) {
                            profileCache[user.uid] = CacheEntry(
                                ProfileData(
                                    username = user.username,
                                    avatarUrl = user.avatarUrl?.let { constructAvatarUrl(it) },
                                    isVerified = user.isVerified ?: false
                                )
                            )
                        }
                    }
                }
            }
            val postsWithReactions = populatePostReactions(posts)
            val postsWithPolls = populatePostPolls(postsWithReactions)
            Result.success(postsWithPolls)
        } catch (e: Exception) {
            Napier.e("Failed to fetch user posts: ${e.message}", e, tag = TAG)
            Result.failure(e)
        }
    }
    suspend fun updatePost(postId: String, updates: Map<String, Any?>): Result<Post> = withContext(Dispatchers.IO) {
        try {
            client.from("posts").update(updates) {
                filter { eq("id", postId) }
            }
            Result.success(Post(id = postId, authorUid = ""))
        } catch (e: Exception) {
            Napier.e("Failed to update post", e, tag = TAG)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }
    suspend fun updatePost(post: Post): Result<Post> = withContext(Dispatchers.IO) {
        try {
            val updateDto = post.toUpdateDto()
            client.from("posts").update(updateDto) {
                filter { eq("id", post.id) }
            }
            storageDatabase.postQueries.insertPost(PostMapper.toEntity(post))
            invalidateCache()
            Result.success(post)
        } catch (e: Exception) {
            Napier.e("Failed to update full post", e, tag = TAG)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }
    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.from("posts").delete {
                filter { eq("id", postId) }
            }
            storageDatabase.postQueries.deleteById(postId)
            invalidateCache()
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to delete post", e, tag = TAG)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    private val reactionRepository = ReactionRepository(client)
    private val pollRepository = PollRepository(client)

    suspend fun toggleReaction(
        postId: String,
        userId: String,
        reactionType: ReactionType,
        oldReaction: ReactionType? = null,
        skipCheck: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {

        reactionRepository.toggleReaction(postId, "post", reactionType, oldReaction, skipCheck)
            .map { Unit }
    }

    suspend fun getReactionSummary(postId: String): Result<Map<ReactionType, Int>> =
        reactionRepository.getReactionSummary(postId, "post")

    suspend fun getUserReaction(postId: String, userId: String): Result<ReactionType?> =
        if (userId == client.auth.currentUserOrNull()?.id) {
             reactionRepository.getUserReaction(postId, "post")
        } else {
             withContext(Dispatchers.IO) {
                 try {
                     val reaction = client.from("reactions")
                         .select { filter { eq("post_id", postId); eq("user_id", userId) } }
                         .decodeSingleOrNull<JsonObject>()
                     val typeStr = reaction?.get("reaction_type")?.jsonPrimitive?.contentOrNull
                     Result.success(typeStr?.let { ReactionType.fromString(it) })
                 } catch (e: Exception) {
                     Result.failure(Exception("Error fetching user reaction"))
                 }
             }
        }

    suspend fun getUsersWhoReacted(
        postId: String,
        reactionType: ReactionType? = null
    ): Result<List<UserReaction>> = withContext(Dispatchers.IO) {
        try {

            val reactions = client.from("reactions")
                .select(Columns.raw("*, users!inner(uid, username, avatar, verify)")) {
                    filter {
                        eq("post_id", postId)
                        if (reactionType != null) eq("reaction_type", reactionType.name)
                    }
                }
                .decodeList<JsonObject>()

            val userReactions = reactions.mapNotNull { reaction ->
                val userId = reaction["user_id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val user = reaction["users"]?.jsonObject
                UserReaction(
                    userId = userId,
                    username = user?.get("username")?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    profileImage = user?.get("avatar")?.jsonPrimitive?.contentOrNull?.let { constructAvatarUrl(it) },
                    isVerified = user?.get("verify")?.jsonPrimitive?.booleanOrNull ?: false,
                    reactionType = reaction["reaction_type"]?.jsonPrimitive?.contentOrNull ?: "LIKE",
                    reactedAt = reaction["created_at"]?.jsonPrimitive?.contentOrNull
                )
            }
            Result.success(userReactions)
        } catch (e: Exception) {
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }


    private suspend fun populatePostReactions(posts: List<Post>): List<Post> {
        return reactionRepository.populatePostReactions(posts)
    }

    private suspend fun populatePostPolls(posts: List<Post>): List<Post> {
        val pollPosts = posts.filter { it.hasPoll == true }
        if (pollPosts.isEmpty()) return posts

        val postIds = pollPosts.map { it.id }


        val userVotesResult = pollRepository.getBatchUserVotes(postIds)
        val userVotes = userVotesResult.getOrNull() ?: emptyMap()


        val pollCountsResult = pollRepository.getBatchPollVotes(postIds)
        val pollCounts = pollCountsResult.getOrNull() ?: emptyMap()

        return posts.map { post ->
            if (post.hasPoll == true) {
                val userVote = userVotes[post.id]
                val counts = pollCounts[post.id] ?: emptyMap()


                val updatedOptions = post.pollOptions?.mapIndexed { index, option ->
                    option.copy(votes = counts[index] ?: 0)
                }

                val updatedPost = post.copy(
                    pollOptions = updatedOptions
                )
                updatedPost.userPollVote = userVote
                updatedPost
            } else {
                post
            }
        }
    }

    private suspend fun fetchUserProfilesBatch(userIds: List<String>) {
        try {
            val users = client.from("users").select {
                filter { isIn("uid", userIds) }
            }.decodeList<JsonObject>()

            users.forEach { user ->
                val uid = user["uid"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val profile = ProfileData(
                    username = user["username"]?.jsonPrimitive?.contentOrNull,
                    avatarUrl = user["avatar"]?.jsonPrimitive?.contentOrNull?.let { constructAvatarUrl(it) },
                    isVerified = user["verify"]?.jsonPrimitive?.booleanOrNull ?: false
                )
                profileCache[uid] = CacheEntry(profile)
            }
        } catch (e: Exception) {
            Napier.e("Failed to batch fetch user profiles", e, tag = TAG)
        }
    }

    private suspend fun fetchUserProfile(userId: String): ProfileData? {

        profileCache[userId]?.let { entry ->
            if (!entry.isExpired()) return entry.data
        }

        return try {
            val user = client.from("users").select {
                filter { eq("uid", userId) }
            }.decodeSingleOrNull<JsonObject>()

            if (user != null) {
                val profile = ProfileData(
                    username = user["username"]?.jsonPrimitive?.contentOrNull,
                    avatarUrl = user["avatar"]?.jsonPrimitive?.contentOrNull?.let { constructAvatarUrl(it) },
                    isVerified = user["verify"]?.jsonPrimitive?.booleanOrNull ?: false
                )
                profileCache[userId] = CacheEntry(profile)
                profile
            } else {
                null
            }
        } catch (e: Exception) {
            Napier.e("Failed to fetch user profile for $userId", e, tag = TAG)
            null
        }
    }

    private suspend fun processMentions(
        postId: String,
        content: String,
        senderId: String
    ) {
        try {
            val mentionedUsers = MentionParser.extractMentions(content)
            if (mentionedUsers.isNotEmpty()) {
                Napier.d("Processing mentions: $mentionedUsers", tag = TAG)
            }
        } catch (e: Exception) {
            Napier.e("Failed to process mentions: ${e.message}", e, tag = TAG)
        }
    }

    suspend fun toggleComments(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val post = client.from("posts").select(Columns.list("post_disable_comments")) {
                filter { eq("id", postId) }
            }.decodeSingleOrNull<JsonObject>()

            val currentStr = post?.get("post_disable_comments")?.jsonPrimitive?.contentOrNull
            val currentBool = currentStr == "true"
            val newStr = if (currentBool) "false" else "true"

            client.from("posts").update(mapOf("post_disable_comments" to newStr)) {
                filter { eq("id", postId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
             Napier.e("Failed to toggle comments", e, tag = TAG)
             Result.failure(e)
        }
    }
}
