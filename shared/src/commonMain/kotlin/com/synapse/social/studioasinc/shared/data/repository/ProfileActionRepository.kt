package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.domain.repository.ProfileActionRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ProfileActionRepository(private val client: SupabaseClient) : ProfileActionRepository {
    override suspend fun blockUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    override suspend fun reportUser(userId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }
}
