package com.synapse.social.studioasinc.shared.domain.repository

interface ReshareRepository {
    suspend fun resharePost(postId: String, userId: String, text: String? = null): Result<Unit>
}
