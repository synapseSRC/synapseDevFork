package com.synapse.social.studioasinc.shared.domain.repository

interface FeedbackRepository {
    suspend fun submitFeedback(userId: String, content: String, type: String): Result<Unit>
}
