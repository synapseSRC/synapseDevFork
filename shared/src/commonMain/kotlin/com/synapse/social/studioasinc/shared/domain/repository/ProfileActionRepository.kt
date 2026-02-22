package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.core.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow

interface ProfileActionRepository {
    suspend fun lockProfile(userId: String, isLocked: Boolean): Result<Unit>
    suspend fun archiveProfile(userId: String, isArchived: Boolean): Result<Unit>
    suspend fun blockUser(userId: String, blockedUserId: String): Result<Unit>
    suspend fun reportUser(userId: String, reportedUserId: String, reason: String): Result<Unit>
    suspend fun muteUser(userId: String, mutedUserId: String): Result<Unit>
}
