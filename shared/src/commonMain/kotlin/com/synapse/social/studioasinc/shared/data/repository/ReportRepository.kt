package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.domain.repository.ReportRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ReportRepository(private val client: SupabaseClient) : ReportRepository {
    override suspend fun createReport(postId: String, reporterId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.from("reports").insert(mapOf("post_id" to postId, "reporter_id" to reporterId, "reason" to reason))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
