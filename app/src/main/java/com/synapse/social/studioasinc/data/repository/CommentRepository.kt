package com.synapse.social.studioasinc.data.repository

import android.util.Log
import com.synapse.social.studioasinc.data.local.database.CommentDao
import com.synapse.social.studioasinc.data.repository.CommentMapper
import com.synapse.social.studioasinc.domain.model.*
import com.synapse.social.studioasinc.domain.model.UserStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import javax.inject.Inject
import java.time.Instant
import com.synapse.social.studioasinc.shared.data.database.Comment as SharedComment

class CommentRepository @Inject constructor(
    private val client: SupabaseClient = com.synapse.social.studioasinc.core.network.SupabaseClient.client,
    private val commentDao: CommentDao,
    private val userRepository: UserRepository
) {
    private val TAG = "CommentRepository"

    suspend fun getComments(postId: String): Result<List<CommentWithUser>> = withContext(Dispatchers.IO) {
        try {
            val response = client.from("comments")
                .select(columns = Columns.raw("*, users(uid, username, display_name, avatar, bio, verify, status, account_type, followers_count, following_count, posts_count, banned)")) {
                    filter {
                        eq("post_id", postId)
                        eq("is_deleted", false)
                        eq("parent_comment_id", null)
                    }
                    order("created_at", Order.ASCENDING)
                }

            val commentsJson = response.decodeList<JsonObject>()
            val comments = commentsJson.mapNotNull { parseCommentFromJson(it) }

            val commentsToCache = comments.map {
                CommentMapper.toEntity(it.toComment(), it.user?.username, it.user?.avatar)
            }
            commentDao.insertAll(commentsToCache)

            Result.success(comments)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch comments: ${e.message}", e)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    suspend fun fetchComments(postId: String, limit: Int, offset: Int): Result<List<CommentWithUser>> = withContext(Dispatchers.IO) {
        try {
            val response = client.from("comments")
                .select(columns = Columns.raw("*, users(uid, username, display_name, avatar, bio, verify, status, account_type, followers_count, following_count, posts_count, banned)")) {
                    filter {
                        eq("post_id", postId)
                        eq("is_deleted", false)
                        eq("parent_comment_id", null)
                    }
                    order("created_at", Order.ASCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }

            val commentsJson = response.decodeList<JsonObject>()
            val comments = commentsJson.mapNotNull { parseCommentFromJson(it) }

            // Note: Caching paged comments might be tricky if we don't clear old ones or handle gaps.
            // For now, we cache what we fetch.
            val commentsToCache = comments.map {
                CommentMapper.toEntity(it.toComment(), it.user?.username, it.user?.avatar)
            }
            commentDao.insertAll(commentsToCache)

            Result.success(comments)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch paged comments: ${e.message}", e)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<CommentWithUser>> {
        return commentDao.getCommentsForPost(postId).map { entities ->
            entities.map { entity ->

                val comment = CommentMapper.toModel(entity)

                // Always fetch user from repository cache as SharedComment doesn't store user details
                val user = try {
                    userRepository.getUserById(entity.authorId).getOrNull()?.let { domainUser ->
                        UserProfile(
                            uid = domainUser.uid,
                            username = domainUser.username ?: "",
                            displayName = domainUser.displayName ?: "",
                            email = domainUser.email ?: "",
                            bio = domainUser.bio,
                            avatar = domainUser.avatar,
                            followersCount = domainUser.followersCount,
                            followingCount = domainUser.followingCount,
                            postsCount = domainUser.postsCount,
                            status = domainUser.status,
                            account_type = domainUser.accountType,
                            verify = domainUser.verify,
                            banned = domainUser.banned
                        )
                    }
                } catch (e: Exception) {
                    null
                }

                CommentWithUser(
                    id = comment.key,
                    postId = comment.postKey,
                    userId = comment.uid,
                    parentCommentId = comment.replyCommentKey,
                    content = comment.comment,
                    mediaUrl = null,
                    createdAt = comment.push_time,
                    updatedAt = null,
                    likesCount = entity.likesCount,
                    repliesCount = entity.repliesCount,
                    isDeleted = entity.isDeleted,
                    isEdited = false,
                    isPinned = false,
                    user = user,
                    reactionSummary = emptyMap(),
                    userReaction = null
                )
            }
        }
    }

    suspend fun createComment(postId: String, content: String, mediaUrl: String? = null, parentId: String? = null): Result<CommentWithUser> {
        return addComment(postId, content, parentId)
    }

    suspend fun addComment(postId: String, content: String, parentCommentId: String? = null): Result<CommentWithUser> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User must be authenticated"))

            if (content.isBlank()) {
                return@withContext Result.failure(Exception("Comment cannot be empty"))
            }

            val commentData = buildJsonObject {
                put("post_id", postId)
                put("user_id", currentUser.id)
                put("content", content)
                if (parentCommentId != null) {
                    put("parent_comment_id", parentCommentId)
                }
                put("created_at", java.time.Instant.now().toString())
                put("updated_at", java.time.Instant.now().toString())
            }

            val response = client.from("comments")
                .insert(commentData) {
                    select(Columns.raw("*, users(uid, username, display_name, avatar, verify)"))
                }
                .decodeSingle<JsonObject>()

            val newComment = parseCommentFromJson(response)
                ?: return@withContext Result.failure(Exception("Failed to parse created comment"))


            val commentEntity = CommentMapper.toEntity(newComment.toComment(), newComment.user?.username, newComment.user?.avatar)
            commentDao.insertAll(listOf(commentEntity))


            if (parentCommentId != null) {
                updateRepliesCount(parentCommentId, 1)
            }
            updatePostCommentsCount(postId, 1)

            processMentions(postId, newComment.id, content, currentUser.id, parentCommentId)

            Result.success(newComment)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add comment: ${e.message}", e)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    suspend fun deleteComment(commentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User must be authenticated"))


            val existingComment = client.from("comments")
                .select { filter { eq("id", commentId) } }
                .decodeSingleOrNull<JsonObject>()
                ?: return@withContext Result.failure(Exception("Comment not found"))

            val commentUserId = existingComment["user_id"]?.jsonPrimitive?.contentOrNull
            if (commentUserId != currentUser.id) {

                val postId = existingComment["post_id"]?.jsonPrimitive?.contentOrNull
                if (postId != null) {
                    val post = client.from("posts")
                        .select { filter { eq("id", postId) } }
                        .decodeSingleOrNull<JsonObject>()
                    val postAuthor = post?.get("author_uid")?.jsonPrimitive?.contentOrNull

                    if (postAuthor != currentUser.id) {
                         return@withContext Result.failure(Exception("Not authorized to delete this comment"))
                    }
                } else {
                    return@withContext Result.failure(Exception("Not authorized to delete this comment"))
                }
            }


            client.from("comments")
                .update({ set("is_deleted", true) }) {
                    filter { eq("id", commentId) }
                }


            val parentId = existingComment["parent_comment_id"]?.jsonPrimitive?.contentOrNull
            if (parentId != null) {
                updateRepliesCount(parentId, -1)
            }
            val postId = existingComment["post_id"]?.jsonPrimitive?.contentOrNull
            if (postId != null) {
                updatePostCommentsCount(postId, -1)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun editComment(commentId: String, content: String): Result<CommentWithUser> {
        return updateComment(commentId, content)
    }

    suspend fun updateComment(commentId: String, newContent: String): Result<CommentWithUser> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User must be authenticated"))

            if (newContent.isBlank()) {
                return@withContext Result.failure(Exception("Comment cannot be empty"))
            }

            val existingComment = client.from("comments")
                .select { filter { eq("id", commentId) } }
                .decodeSingleOrNull<JsonObject>()
                ?: return@withContext Result.failure(Exception("Comment not found"))

            val commentUserId = existingComment["user_id"]?.jsonPrimitive?.contentOrNull
            if (commentUserId != currentUser.id) {
                return@withContext Result.failure(Exception("Not authorized to edit this comment"))
            }

            val response = client.from("comments")
                .update({
                    set("content", newContent)
                    set("is_edited", true)
                    set("edited_at", java.time.Instant.now().toString())
                }) {
                    filter { eq("id", commentId) }
                    select(Columns.raw("*, users(uid, username, display_name, avatar, verify)"))
                }
                .decodeSingle<JsonObject>()

            val updatedComment = parseCommentFromJson(response)
                ?: return@withContext Result.failure(Exception("Failed to parse updated comment"))


            val commentEntity = CommentMapper.toEntity(updatedComment.toComment(), updatedComment.user?.username, updatedComment.user?.avatar)
            commentDao.insertAll(listOf(commentEntity))

            Result.success(updatedComment)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun pinComment(commentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User must be authenticated"))

            val existingComment = client.from("comments")
                .select { filter { eq("id", commentId) } }
                .decodeSingleOrNull<JsonObject>()
                ?: return@withContext Result.failure(Exception("Comment not found"))

            val postId = existingComment["post_id"]?.jsonPrimitive?.contentOrNull
                ?: return@withContext Result.failure(Exception("Post ID not found for comment"))

            val post = client.from("posts")
                .select { filter { eq("id", postId) } }
                .decodeSingleOrNull<JsonObject>()
                ?: return@withContext Result.failure(Exception("Post not found"))

            val postAuthor = post["author_uid"]?.jsonPrimitive?.contentOrNull
            if (postAuthor != currentUser.id) {
                return@withContext Result.failure(Exception("Only post author can pin comments"))
            }

            client.from("comments")
                .update({ set("is_pinned", true) }) {
                    filter { eq("id", commentId) }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pin comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun hideComment(commentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User must be authenticated"))

            val existingComment = client.from("comments")
                .select { filter { eq("id", commentId) } }
                .decodeSingleOrNull<JsonObject>()
                ?: return@withContext Result.failure(Exception("Comment not found"))

            val commentUserId = existingComment["user_id"]?.jsonPrimitive?.contentOrNull


            var isAuthorized = commentUserId == currentUser.id

            if (!isAuthorized) {
                val postId = existingComment["post_id"]?.jsonPrimitive?.contentOrNull
                if (postId != null) {

                    val post = client.from("posts")
                        .select { filter { eq("id", postId) } }
                        .decodeSingleOrNull<JsonObject>()
                    val postAuthor = post?.get("author_uid")?.jsonPrimitive?.contentOrNull
                    isAuthorized = postAuthor == currentUser.id
                }
            }

            if (!isAuthorized) {
                return@withContext Result.failure(Exception("Not authorized to hide this comment"))
            }

            client.from("comments")
                .update({
                    set("is_hidden", true)
                    set("hidden_by", currentUser.id)
                }) {
                    filter { eq("id", commentId) }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun reportComment(commentId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User must be authenticated"))

            client.from("comment_reports")
                .insert(buildJsonObject {
                    put("comment_id", commentId)
                    put("reporter_id", currentUser.id)
                    put("reason", reason)
                    put("created_at", java.time.Instant.now().toString())
                })

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to report comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getReplies(commentId: String): Result<List<CommentWithUser>> = withContext(Dispatchers.IO) {
        try {
            val response = client.from("comments")
                .select(columns = Columns.raw("*, users(uid, username, display_name, avatar, bio, verify, status, account_type, followers_count, following_count, posts_count, banned)")) {
                    filter {
                        eq("parent_comment_id", commentId)
                        eq("is_deleted", false)
                    }
                    order("created_at", Order.ASCENDING)
                }

            val commentsJson = response.decodeList<JsonObject>()
            val comments = commentsJson.mapNotNull { parseCommentFromJson(it) }

            // Note: replies are not cached in local db for now as Comment table structure is flat and getCommentsForPost only fetches top level (if filtered by parentId=null there).
            // But actually we store all comments. CommentDao doesn't enforce hierarchy.
            // But insertAll is good.
            val commentsToCache = comments.map {
                CommentMapper.toEntity(it.toComment(), it.user?.username, it.user?.avatar)
            }
            commentDao.insertAll(commentsToCache)

            Result.success(comments)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch replies: ${e.message}", e)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    private suspend fun parseCommentFromJson(data: JsonObject): CommentWithUser? {
        return try {
            val user = parseUserProfileFromJson(data["users"]?.jsonObject)
            val commentId = data["id"]?.jsonPrimitive?.contentOrNull ?: return null

            val reactionSummary = emptyMap<ReactionType, Int>()
            val userReaction = null

            CommentWithUser(
                id = commentId,
                postId = data["post_id"]?.jsonPrimitive?.contentOrNull ?: return null,
                userId = data["user_id"]?.jsonPrimitive?.contentOrNull ?: return null,
                parentCommentId = data["parent_comment_id"]?.jsonPrimitive?.contentOrNull,
                content = data["content"]?.jsonPrimitive?.contentOrNull ?: "",
                mediaUrl = data["media_url"]?.jsonPrimitive?.contentOrNull,
                createdAt = data["created_at"]?.jsonPrimitive?.contentOrNull ?: "",
                updatedAt = data["updated_at"]?.jsonPrimitive?.contentOrNull,
                likesCount = data["likes_count"]?.jsonPrimitive?.intOrNull ?: 0,
                repliesCount = data["replies_count"]?.jsonPrimitive?.intOrNull ?: 0,
                isDeleted = data["is_deleted"]?.jsonPrimitive?.booleanOrNull ?: false,
                isEdited = data["is_edited"]?.jsonPrimitive?.booleanOrNull ?: false,
                isPinned = data["is_pinned"]?.jsonPrimitive?.booleanOrNull ?: false,
                user = user,
                reactionSummary = reactionSummary,
                userReaction = userReaction
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse comment: ${e.message}")
            null
        }
    }

    private fun parseUserProfileFromJson(userData: JsonObject?): UserProfile? {
        if (userData == null) return null

        return try {
            UserProfile(
                uid = userData["uid"]?.jsonPrimitive?.contentOrNull ?: return null,
                username = userData["username"]?.jsonPrimitive?.contentOrNull ?: "",
                displayName = userData["display_name"]?.jsonPrimitive?.contentOrNull ?: "",
                email = "",
                bio = userData["bio"]?.jsonPrimitive?.contentOrNull,
                avatar = userData["avatar"]?.jsonPrimitive?.contentOrNull,
                followersCount = userData["followers_count"]?.jsonPrimitive?.intOrNull ?: 0,
                followingCount = userData["following_count"]?.jsonPrimitive?.intOrNull ?: 0,
                postsCount = userData["posts_count"]?.jsonPrimitive?.intOrNull ?: 0,
                status = UserStatus.fromString(userData["status"]?.jsonPrimitive?.contentOrNull),
                account_type = userData["account_type"]?.jsonPrimitive?.contentOrNull ?: "user",
                verify = userData["verify"]?.jsonPrimitive?.booleanOrNull ?: false,
                banned = userData["banned"]?.jsonPrimitive?.booleanOrNull ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse user profile: ${e.message}")
            null
        }
    }

    private suspend fun updateRepliesCount(commentId: String, delta: Int) {
        try {
            val actualCount = client.from("comments")
                .select(columns = Columns.raw("")) {
                    filter {
                        eq("parent_comment_id", commentId)
                        eq("is_deleted", false)
                    }
                    count(Count.EXACT)
                }
                .countOrNull() ?: 0

            client.from("comments")
                .update({ set("replies_count", actualCount) }) {
                    filter { eq("id", commentId) }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update replies count: ${e.message}")
        }
    }

    private suspend fun updatePostCommentsCount(postId: String, delta: Int) {
        try {
            val post = client.from("posts")
                .select { filter { eq("id", postId) } }
                .decodeSingleOrNull<JsonObject>()

            val currentCount = post?.get("comments_count")?.jsonPrimitive?.intOrNull ?: 0
            val newCount = maxOf(0, currentCount + delta)

            client.from("posts")
                .update({ set("comments_count", newCount) }) {
                    filter { eq("id", postId) }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update post comments count: ${e.message}")
        }
    }

    private fun mapSupabaseError(exception: Exception): String {
        val message = exception.message ?: "Unknown error"

        Log.e(TAG, "Supabase error: $message", exception)

        return when {
            message.contains("PGRST200") -> "Database table not found"
            message.contains("PGRST100") -> "Database column does not exist"
            message.contains("PGRST116") -> "Comment not found"
            message.contains("relation", ignoreCase = true) -> "Database table does not exist"
            message.contains("column", ignoreCase = true) -> "Database column mismatch"
            message.contains("policy", ignoreCase = true) || message.contains("rls", ignoreCase = true) ->
                "Permission denied"
            message.contains("connection", ignoreCase = true) || message.contains("network", ignoreCase = true) ->
                "Connection failed. Please check your internet connection."
            message.contains("timeout", ignoreCase = true) -> "Request timed out. Please try again."
            message.contains("unauthorized", ignoreCase = true) -> "Permission denied."
            message.contains("54001") -> "Server Configuration Error: Stack depth limit exceeded. Please contact support."
            else -> "Failed to process comment: $message"
        }
    }

    private suspend fun processMentions(
        postId: String,
        commentId: String,
        content: String,
        senderId: String,
        parentCommentId: String?
    ) {
        try {
            val mentionedUsers = com.synapse.social.studioasinc.core.domain.parser.MentionParser.extractMentions(content)

            if (mentionedUsers.isNotEmpty()) {
                Log.d(TAG, "Processing mentions: $mentionedUsers")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process mentions: ${e.message}", e)
        }
    }
}

private fun CommentWithUser.toComment(): Comment {
    return Comment(
        key = this.id,
        postKey = this.postId,
        uid = this.userId,
        comment = this.content,
        push_time = this.createdAt.toString(),
        replyCommentKey = this.parentCommentId
    )
}
