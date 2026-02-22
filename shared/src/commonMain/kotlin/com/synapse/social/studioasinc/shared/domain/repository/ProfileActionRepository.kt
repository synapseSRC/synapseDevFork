package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.*

interface ProfileActionRepository {
    suspend fun blockUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun reportUser(userId: String, targetUserId: String, reason: String): Result<Unit>
    suspend fun muteUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun archiveProfile(userId: String): Result<Unit>
    suspend fun lockProfile(userId: String, lock: Boolean): Result<Unit>
}
