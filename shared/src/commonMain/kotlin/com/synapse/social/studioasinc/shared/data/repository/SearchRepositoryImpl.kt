package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.domain.model.SearchAccount
import com.synapse.social.studioasinc.shared.domain.model.SearchHashtag
import com.synapse.social.studioasinc.shared.domain.model.SearchNews
import com.synapse.social.studioasinc.shared.domain.model.SearchPost
import com.synapse.social.studioasinc.shared.domain.repository.ISearchRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.SupabaseClient as SupabaseClientInterface

@Serializable
private data class PostDto(
    val id: String,
    val post_text: String? = null,
    val author_uid: String,
    val likes_count: Int = 0,
    val comments_count: Int = 0,
    val reshares_count: Int = 0,
    val created_at: String,
    val author: AuthorDto? = null
)

@Serializable
private data class AuthorDto(
    val display_name: String? = null,
    val username: String? = null,
    val avatar: String? = null
)

class SearchRepositoryImpl(
    private val client: SupabaseClientInterface = SupabaseClient.client
) : ISearchRepository {

    override suspend fun searchPosts(query: String): Result<List<SearchPost>> = runCatching {
        // Using 'users' as the relationship name. If it fails, we fallback to non-joined and empty author data.
        val columns = Columns.raw("id, post_text, author_uid, likes_count, comments_count, reshares_count, created_at, author:users!posts_author_uid_fkey(display_name, username, avatar)")

        val result = client.postgrest["posts"].select(columns = columns) {
            if (query.isNotBlank()) {
                filter {
                    ilike("post_text", "%$query%")
                }
            }
            order("created_at", Order.DESCENDING)
            limit(20)
        }.decodeList<PostDto>()

        result.map { dto ->
            SearchPost(
                id = dto.id,
                content = dto.post_text,
                authorId = dto.author_uid,
                likesCount = dto.likes_count,
                commentsCount = dto.comments_count,
                boostCount = dto.reshares_count,
                createdAt = dto.created_at,
                authorName = dto.author?.display_name,
                authorHandle = dto.author?.username,
                authorAvatar = dto.author?.avatar
            )
        }
    }

    override suspend fun searchHashtags(query: String): Result<List<SearchHashtag>> = runCatching {
        client.postgrest["hashtags"].select {
            if (query.isNotBlank()) {
                filter {
                    ilike("tag", "$query%")
                }
            }
            order("usage_count", Order.DESCENDING)
            limit(20)
        }.decodeList<SearchHashtag>().map {
            // Mock sparkline data
            it.copy(sparklinePoints = List(10) { _ -> (0..100).random().toFloat() })
        }
    }

    override suspend fun getTrendingHashtags(): Result<List<SearchHashtag>> = runCatching {
        client.postgrest["hashtags"].select {
            order("usage_count", Order.DESCENDING)
            limit(10)
        }.decodeList<SearchHashtag>().map {
            it.copy(sparklinePoints = List(10) { _ -> (0..100).random().toFloat() })
        }
    }

    override suspend fun searchNews(query: String): Result<List<SearchNews>> = runCatching {
        client.postgrest["news_articles"].select {
            if (query.isNotBlank()) {
                filter {
                    ilike("headline", "%$query%")
                }
            }
            order("published_at", Order.DESCENDING)
            limit(20)
        }.decodeList()
    }

    override suspend fun getSuggestedAccounts(query: String): Result<List<SearchAccount>> = runCatching {
        client.postgrest["users"].select {
            if (query.isNotBlank()) {
                filter {
                    or {
                        ilike("username", "%$query%")
                        ilike("display_name", "%$query%")
                    }
                }
            } else {
                 // Suggest verified or high follower accounts if query is empty
                 order("followers_count", Order.DESCENDING)
            }
            limit(20)
        }.decodeList()
    }
}
