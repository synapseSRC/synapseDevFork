package com.synapse.social.studioasinc.shared.domain.repository

import android.util.Log
import com.synapse.social.studioasinc.shared.domain.model.PollOption
import com.synapse.social.studioasinc.shared.domain.model.PollOptionResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow

interface PollRepository {
    suspend fun getUserVote(postId: String): Result<Int?>
    suspend fun submitVote(postId: String, optionIndex: Int): Result<Unit>
    suspend fun revokeVote(postId: String): Result<Unit>
    suspend fun getPollResults(postId: String): Result<List<PollOptionResult>>
    suspend fun getBatchUserVotes(postIds: List<String>): Result<Map<String, Int>>
    suspend fun getBatchPollVotes(postIds: List<String>): Result<Map<String, Map<Int, Int>>>
}
