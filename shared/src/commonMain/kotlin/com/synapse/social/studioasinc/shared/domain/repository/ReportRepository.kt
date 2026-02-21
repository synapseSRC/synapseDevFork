package com.synapse.social.studioasinc.shared.domain.repository

interface ReportRepository {
    suspend fun createReport(postId: String, reporterId: String, reason: String): Result<Unit>
}
