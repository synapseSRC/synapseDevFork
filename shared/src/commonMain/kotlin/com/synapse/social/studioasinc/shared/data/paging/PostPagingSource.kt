package com.synapse.social.studioasinc.shared.data.paging

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.repository.ReactionRepository
import com.synapse.social.studioasinc.shared.domain.model.Post
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import kotlinx.serialization.json.*

class PostPagingSource(
    private val queryBuilder: PostgrestQueryBuilder
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val reactionRepository = ReactionRepository()

    suspend fun loadPage(position: Int, pageSize: Int): Result<List<Post>> = runCatching {
        val response = queryBuilder
            .select(
                columns = Columns.raw("""
                    *,
                    users!posts_author_uid_fkey(username, avatar, verify),
                    latest_comments:comments(id, content, user_id, created_at, users(username))
                """.trimIndent())
            ) {
                order("timestamp", order = Order.DESCENDING)
                range(position.toLong(), (position + pageSize - 1).toLong())
            }
            .decodeList<JsonObject>()

        val parsedPosts = response.map { jsonElement ->
            val post = json.decodeFromJsonElement<Post>(jsonElement)
            val userData = jsonElement["users"]?.jsonObject
            post.username = userData?.get("username")?.jsonPrimitive?.contentOrNull
            post.avatarUrl = userData?.get("avatar")?.jsonPrimitive?.contentOrNull?.let { avatarPath ->
                SupabaseClient.constructStorageUrl(SupabaseClient.BUCKET_USER_AVATARS, avatarPath)
            }
            post.isVerified = userData?.get("verify")?.jsonPrimitive?.booleanOrNull ?: false

            val commentsArray = jsonElement["latest_comments"]?.jsonArray
            if (!commentsArray.isNullOrEmpty()) {
                val latestComment = commentsArray.map { it.jsonObject }
                    .maxByOrNull { it["created_at"]?.jsonPrimitive?.contentOrNull ?: "" }

                if (latestComment != null) {
                    post.latestCommentText = latestComment["content"]?.jsonPrimitive?.contentOrNull
                    val commentUser = latestComment["users"]?.jsonObject
                    post.latestCommentAuthor = commentUser?.get("username")?.jsonPrimitive?.contentOrNull
                }
            }
            post
        }

        reactionRepository.populatePostReactions(parsedPosts)
    }
}
