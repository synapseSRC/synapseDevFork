package com.synapse.social.studioasinc.shared.core.network

import com.synapse.social.studioasinc.shared.core.config.SynapseConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.aakira.napier.Napier
import io.ktor.client.plugins.HttpTimeout



object SupabaseClient {
    private const val TAG = "SupabaseClient"

    @OptIn(SupabaseInternal::class)
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
                install(Auth)
                install(Postgrest)
                install(Realtime)
                install(Functions)
                install(Storage) {
                    if (SynapseConfig.SUPABASE_SYNAPSE_S3_ENDPOINT_URL.isNotBlank()) {
                        customUrl = SynapseConfig.SUPABASE_SYNAPSE_S3_ENDPOINT_URL
                    }
                }

                httpConfig {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 300_000
                        connectTimeoutMillis = 60_000
                        socketTimeoutMillis = 300_000
                    }
                }
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
    const val TABLE_USERS = "users"
}
