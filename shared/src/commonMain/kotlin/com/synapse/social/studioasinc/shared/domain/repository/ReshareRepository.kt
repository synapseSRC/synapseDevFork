package com.synapse.social.studioasinc.shared.domain.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow

interface ReshareRepository {
    suspend fun hasReshared(postId: String): Result<Boolean>
    suspend fun createReshare(postId: String, commentary: String?
}
