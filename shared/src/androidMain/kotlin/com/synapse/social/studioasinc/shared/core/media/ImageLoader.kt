package com.synapse.social.studioasinc.shared.core.media

actual class ImageLoader {
    actual suspend fun loadImage(url: String): Result<ByteArray> {
        return Result.failure(NotImplementedError("ImageLoader not implemented for Android"))
    }

    actual suspend fun preloadImage(url: String): Result<Unit> {
        return Result.failure(NotImplementedError("ImageLoader not implemented for Android"))
    }

    actual fun clearMemoryCache() {
        // Stub
    }

    actual fun clearDiskCache() {
        // Stub
    }
}
