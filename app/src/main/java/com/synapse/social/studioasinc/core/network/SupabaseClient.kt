package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.core.network

import android.util.Log
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.BuildConfig
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.shared.core.network.SupabaseClient as SharedSupabaseClient
import java.net.URL
import java.net.MalformedURLException

/**
 * Exception thrown when Supabase configuration is invalid or missing.
 */
class ConfigurationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Supabase client singleton for the application.
 * Provides centralized access to Supabase services including Auth, Postgrest, Realtime, and Storage.
 *
 * ARCHITECTURAL NOTE:
 * This class is now a facade over the Shared Module's SupabaseClient to ensure
 * unified session state management across the application.
 */
object SupabaseClient {
    private const val TAG = "SupabaseClient"

    /**
     * Callback to open a URL in the browser.
     * Kept for backward compatibility but currently unused by Shared Client.
     */
    var openUrl: ((String) -> Unit)? = null

    /**
     * Lazy-initialized Supabase client instance.
     * Delegates to the Shared Module's client to ensure single source of truth.
     */
    val client by lazy {
        try {
            if (!isConfigured()) {
                Log.e(TAG, "CRITICAL: Supabase credentials not configured!")
                throw ConfigurationException(
                    "Supabase not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY in gradle.properties"
                )
            }
            // Delegate to Shared Client
            SharedSupabaseClient.client
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Supabase client via Shared module: ${e.message}", e)
            throw ConfigurationException("Failed to initialize Supabase client", e)
        }
    }

    /**
     * Check if Supabase is properly configured with valid credentials.
     * @return true if both URL and API key are configured, false otherwise
     */
    fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.isNotBlank() &&
               BuildConfig.SUPABASE_URL != "https://your-project.supabase.co" &&
               BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
               BuildConfig.SUPABASE_ANON_KEY != "your-anon-key-here"
    }

    /**
     * Get the configured Supabase URL.
     * @return The Supabase project URL
     */
    fun getUrl(): String = BuildConfig.SUPABASE_URL

    const val BUCKET_POST_MEDIA = "posts"
    const val BUCKET_USER_AVATARS = "avatars"
    const val BUCKET_USER_COVERS = "covers"

    /**
     * Construct a full Supabase Storage URL from a storage path.
     *
     * @param bucket The storage bucket name
     * @param path The path within the storage bucket
     * @return Full public URL for the storage object
     */
    fun constructStorageUrl(bucket: String, path: String): String {
        // Validate URL format - Ensure BuildConfig.SUPABASE_URL is a valid URL before concatenation
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

    /**
     * Constructs URL for post media storage
     */
    fun constructMediaUrl(storagePath: String): String {
        return constructStorageUrl(BUCKET_POST_MEDIA, storagePath)
    }

    /**
     * Constructs URL for user avatar storage
     */
    fun constructAvatarUrl(storagePath: String): String {
        return constructStorageUrl(BUCKET_USER_AVATARS, storagePath)
    }
}
