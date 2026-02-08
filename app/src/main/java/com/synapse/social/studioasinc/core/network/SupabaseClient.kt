package com.synapse.social.studioasinc.core.network

import android.util.Log
import com.synapse.social.studioasinc.BuildConfig
import com.synapse.social.studioasinc.shared.core.network.SupabaseClient as SharedSupabaseClient
import java.net.URL
import java.net.MalformedURLException



class ConfigurationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)



object SupabaseClient {
    private const val TAG = "SupabaseClient"



    var openUrl: ((String) -> Unit)? = null



    val client by lazy {
        try {
            if (!isConfigured()) {
                Log.e(TAG, "CRITICAL: Supabase credentials not configured!")
                throw ConfigurationException(
                    "Supabase not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY in gradle.properties"
                )
            }

            SharedSupabaseClient.client
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Supabase client via Shared module: ${e.message}", e)
            throw ConfigurationException("Failed to initialize Supabase client", e)
        }
    }



    fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.isNotBlank() &&
               BuildConfig.SUPABASE_URL != "https://your-project.supabase.co" &&
               BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
               BuildConfig.SUPABASE_ANON_KEY != "your-anon-key-here"
    }



    fun getUrl(): String = BuildConfig.SUPABASE_URL

    const val BUCKET_POST_MEDIA = "posts"
    const val BUCKET_USER_AVATARS = "avatars"
    const val BUCKET_USER_COVERS = "covers"



    fun constructStorageUrl(bucket: String, path: String): String {

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        val supabaseUrl = BuildConfig.SUPABASE_URL
        try {
            URL(supabaseUrl)
        } catch (e: MalformedURLException) {
            throw ConfigurationException("Invalid Supabase URL configured: $supabaseUrl", e)
        }
        return "$supabaseUrl/storage/v1/object/public/$bucket/$path"
    }



    fun constructMediaUrl(storagePath: String): String {
        return constructStorageUrl(BUCKET_POST_MEDIA, storagePath)
    }



    fun constructAvatarUrl(storagePath: String): String {
        return constructStorageUrl(BUCKET_USER_AVATARS, storagePath)
    }
}
