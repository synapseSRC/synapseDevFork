package com.synapse.social.studioasinc.shared.domain.repository

interface ReportRepository {
    suspend fun reportPost(postId: String, reason: String): Result<Unit>
    suspend fun reportUser(userId: String, reason: String): Result<Unit>
}
