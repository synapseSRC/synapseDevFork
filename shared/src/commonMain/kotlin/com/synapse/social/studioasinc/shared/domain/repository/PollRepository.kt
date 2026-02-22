package com.synapse.social.studioasinc.shared.domain.repository

interface PollRepository {
    suspend fun submitVote(postId: String, optionIndex: Int): Result<Unit>
    suspend fun revokeVote(postId: String): Result<Unit>
}
