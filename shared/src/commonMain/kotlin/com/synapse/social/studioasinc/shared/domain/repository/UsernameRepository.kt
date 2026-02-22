package com.synapse.social.studioasinc.shared.domain.repository

import android.util.LruCache
import com.synapse.social.studioasinc.core.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.flow.Flow

interface UsernameRepository {
    suspend fun checkAvailability(username: String): Result<Boolean>
    fun clearCache()
}
