package com.synapse.social.studioasinc.shared.data.repository

import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable




class ReportRepository(
    private val client: SupabaseClient = com.synapse.social.studioasinc.shared.core.network.SupabaseClient.client
) : com.synapse.social.studioasinc.shared.domain.repository.ReportRepository {

    @Serializable
    private data class PostReport(
        val id: String? = null,
        @SerialName("post_id") val postId: String,
        @SerialName("reporter_id") val reporterId: String,
        val reason: String,
        val description: String? = null
    )



    override suspend fun createReport(
        postId: String,
        reporterId: String,
        reason: String
    ): Result<Unit> = runCatching {
        client.from("post_reports")
            .insert(PostReport(
                postId = postId,
                reporterId = reporterId,
                reason = reason,
                description = null
            ))

        Napier.d("Report created: post=$postId, reason=$reason")
    }

    companion object {
        private const val TAG = "ReportRepository"
    }
}
