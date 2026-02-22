package com.synapse.social.studioasinc.shared.domain.repository

interface ProfileActionRepository {
    suspend fun blockUser(userId: String): Result<Unit>
    suspend fun reportUser(userId: String, reason: String): Result<Unit>
}
