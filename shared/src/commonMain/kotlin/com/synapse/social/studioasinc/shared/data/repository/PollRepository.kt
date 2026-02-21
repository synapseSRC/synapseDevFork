package com.synapse.social.studioasinc.shared.data.repository

import io.github.aakira.napier.Napier
import com.synapse.social.studioasinc.shared.domain.model.PollOption
import com.synapse.social.studioasinc.shared.domain.model.PollOptionResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock




class PollRepository(
    private val client: SupabaseClient = com.synapse.social.studioasinc.shared.core.network.SupabaseClient.client
) : com.synapse.social.studioasinc.shared.domain.repository.PollRepository {

    @Serializable
    private data class PollVote(
        val id: String? = null,
        @SerialName("post_id") val postId: String,
        @SerialName("user_id") val userId: String,
        @SerialName("option_index") val optionIndex: Int,
        @SerialName("created_at") val createdAt: String? = null
    )

    @Serializable
    private data class PollVoteCount(
        @SerialName("post_id") val postId: String,
        @SerialName("option_index") val optionIndex: Int,
        @SerialName("vote_count") val voteCount: Long
    )

    @Serializable
    private data class PostPollData(
        val id: String,
        @SerialName("poll_options") val pollOptions: List<PollOption>,
        @SerialName("poll_end_time") val pollEndTime: String?
    )

    override suspend fun submitVote(postId: String, optionIndex: Int): Result<Unit> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return Result.failure(Exception("Not authenticated"))

        val vote = PollVote(
            postId = postId,
            userId = userId,
            optionIndex = optionIndex
        )
        
        client.from("poll_votes")
            .insert(vote)
    }

    suspend fun getUserVote(postId: String): Result<Int?> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return Result.failure(Exception("Not authenticated"))

        val votes = client.from("poll_votes")
            .select(Columns.list("option_index")) {
                filter {
                    eq("post_id", postId)
                    eq("user_id", userId)
                }
            }
            .decodeList<PollVote>()

        votes.firstOrNull()?.optionIndex
    }






    override suspend fun revokeVote(postId: String): Result<Unit> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return Result.failure(Exception("Not authenticated"))


        client.from("poll_votes").delete {
            filter {
                eq("post_id", postId)
                eq("user_id", userId)
            }
        }

        Napier.d("Vote revoked: post=$postId")
    }



    suspend fun getPollResults(postId: String): Result<List<PollOptionResult>> = runCatching {

        val post = client.from("posts")
            .select(Columns.list("poll_options")) {
                filter { eq("id", postId) }
            }
            .decodeSingle<PostPollData>()


        val votes = client.from("poll_votes")
            .select(Columns.list("option_index")) {
                filter { eq("post_id", postId) }
            }
            .decodeList<PollVote>()


        val voteCounts = votes.groupingBy { it.optionIndex }.eachCount()

        PollOptionResult.calculateResults(post.pollOptions.map { it.text }, voteCounts)
    }





    suspend fun getBatchUserVotes(postIds: List<String>): Result<Map<String, Int>> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return Result.failure(Exception("Not authenticated"))

        if (postIds.isEmpty()) return Result.success(emptyMap())

        val votes = client.from("poll_votes")
            .select(Columns.list("post_id", "option_index")) {
                filter {
                    isIn("post_id", postIds)
                    eq("user_id", userId)
                }
            }
            .decodeList<PollVote>()

        votes.associate { it.postId to it.optionIndex }
    }



    suspend fun getBatchPollVotes(postIds: List<String>): Result<Map<String, Map<Int, Int>>> = runCatching {
        if (postIds.isEmpty()) return Result.success(emptyMap())

        val counts = client.postgrest.rpc(
            "get_poll_votes_count",
            mapOf("post_ids" to postIds)
        ).decodeList<PollVoteCount>()

        counts.groupBy { it.postId }
            .mapValues { (_, postCounts) ->
                // Clamping to Int.MAX_VALUE to prevent negative numbers on overflow, preserving best effort count
                postCounts.associate { it.optionIndex to it.voteCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt() }
            }
    }

    companion object {
        private const val TAG = "PollRepository"
    }
}
