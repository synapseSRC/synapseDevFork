package com.synapse.social.studioasinc.shared.core.media

import com.synapse.social.studioasinc.shared.core.util.RetryHandler

expect class ImageLoader {
    suspend fun loadImage(url: String): Result<ByteArray>
    suspend fun preloadImage(url: String): Result<Unit>
    fun clearMemoryCache()
    fun clearDiskCache()
}

class SharedImageLoader(private val platformLoader: ImageLoader) {
    private val cache = MediaCache()

    suspend fun loadImageWithRetry(url: String): Result<ByteArray> {
        // Check cache first
        cache.get(generateCacheKey(url))?.let { cachedData ->
            return Result.success(cachedData)
        }

        // Load with retry logic
        val result = RetryHandler.executeWithRetryResult { _ ->
            platformLoader.loadImage(url)
        }

        return when (result) {
            is RetryHandler.RetryResult.Success -> {
                // Cache the result
                cache.put(generateCacheKey(url), result.value)
                Result.success(result.value)
            }
            is RetryHandler.RetryResult.Failure -> {
                Result.failure(result.exception)
            }
        }
    }

    suspend fun preloadImage(url: String): Result<Unit> {
        return platformLoader.preloadImage(url)
    }

    fun clearCaches() {
        cache.clear()
        platformLoader.clearMemoryCache()
        platformLoader.clearDiskCache()
    }

    private fun generateCacheKey(url: String): String {
        return url.hashCode().toString()
    }
}
