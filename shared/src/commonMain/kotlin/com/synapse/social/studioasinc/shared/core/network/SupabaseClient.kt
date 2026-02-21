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
    const val BUCKET_POST_MEDIA = "posts"
    const val BUCKET_USER_AVATARS = "avatars"
    const val BUCKET_USER_COVERS = "covers"

    fun constructStorageUrl(bucket: String, path: String): String {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        val supabaseUrl = SynapseConfig.SUPABASE_URL
        val baseUrl = if (supabaseUrl.endsWith("/")) supabaseUrl.dropLast(1) else supabaseUrl
        val cleanPath = if (path.startsWith("/")) path.drop(1) else path

        return "$baseUrl/storage/v1/object/public/$bucket/$cleanPath"
    }

    fun constructMediaUrl(storagePath: String): String {
        return constructStorageUrl(BUCKET_POST_MEDIA, storagePath)
    }

    fun constructAvatarUrl(storagePath: String): String {
        return constructStorageUrl(BUCKET_USER_AVATARS, storagePath)
    }
}
