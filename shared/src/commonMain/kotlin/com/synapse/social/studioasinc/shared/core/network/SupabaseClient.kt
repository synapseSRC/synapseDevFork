package com.synapse.social.studioasinc.shared.core.network

import com.synapse.social.studioasinc.shared.core.config.SynapseConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.aakira.napier.Napier

/**
 * Shared Supabase Client
 */
object SupabaseClient {
    private const val TAG = "SupabaseClient"

    val client by lazy {
        try {
            if (SynapseConfig.SUPABASE_URL.isBlank() || SynapseConfig.SUPABASE_ANON_KEY.isBlank()) {
                Napier.e("Supabase credentials not configured!", tag = TAG)
                throw RuntimeException("Supabase not configured.")
            }

            createSupabaseClient(
                supabaseUrl = SynapseConfig.SUPABASE_URL,
                supabaseKey = SynapseConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth) {
                    // FlowType.PKCE is default for mobile
                }
                install(Postgrest)
                install(Realtime)
                install(Storage) {
                    if (SynapseConfig.SUPABASE_SYNAPSE_S3_ENDPOINT_URL.isNotBlank()) {
                        customUrl = SynapseConfig.SUPABASE_SYNAPSE_S3_ENDPOINT_URL
                    }
                }
                // HttpEngine is automatically selected by Ktor based on dependencies (OkHttp for Android, Darwin for iOS)
            }
        } catch (e: Exception) {
            Napier.e("Failed to initialize Supabase client: ${e.message}", e, tag = TAG)
            throw e
        }
    }

    fun isConfigured(): Boolean {
        return SynapseConfig.SUPABASE_URL.isNotBlank() &&
               SynapseConfig.SUPABASE_ANON_KEY.isNotBlank()
    }
}
