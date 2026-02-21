package com.synapse.social.studioasinc.shared.core.media

import com.synapse.social.studioasinc.shared.core.util.TimeUtils
import kotlinx.datetime.Clock

class MediaCache {
    private val cache = mutableMapOf<String, CacheEntry>()
    private val metadata = mutableMapOf<String, FileMetadata>()

    data class CacheEntry(
        val data: ByteArray,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    )

    data class FileMetadata(
        val size: Long,
        val lastAccessed: Long,
        val accessCount: Int = 0
    )

    data class CacheStats(
        val totalSize: Long,
        val fileCount: Int,
        val hitRate: Double
    )

    fun put(key: String, data: ByteArray): Boolean {
        return try {
            cache[key] = CacheEntry(data)
            updateFileMetadata(key, data.size.toLong())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun get(key: String): ByteArray? {
        val entry = cache[key] ?: return null
        updateLastAccessTime(key)
        return entry.data
    }

    fun remove(key: String): Boolean {
        cache.remove(key)
        metadata.remove(key)
        return true
    }

    fun clear() {
        cache.clear()
        metadata.clear()
    }

    fun contains(key: String): Boolean = cache.containsKey(key)

    fun getCacheSize(): Long = metadata.values.sumOf { it.size }

    fun getCacheFileCount(): Int = cache.size

    fun getCacheStats(): CacheStats {
        val totalSize = getCacheSize()
        val fileCount = getCacheFileCount()
        val hitRate = calculateHitRate()
        return CacheStats(totalSize, fileCount, hitRate)
    }

    private fun updateFileMetadata(key: String, size: Long) {
        val current = metadata[key]
        metadata[key] = FileMetadata(
            size = size,
            lastAccessed = Clock.System.now().toEpochMilliseconds(),
            accessCount = (current?.accessCount ?: 0) + 1
        )
    }

    private fun updateLastAccessTime(key: String) {
        val current = metadata[key] ?: return
        metadata[key] = current.copy(
            lastAccessed = Clock.System.now().toEpochMilliseconds(),
            accessCount = current.accessCount + 1
        )
    }

    private fun calculateHitRate(): Double {
        // Simplified hit rate calculation
        return if (metadata.isEmpty()) 0.0 else {
            metadata.values.map { it.accessCount }.average()
        }
    }

    fun evictExpired(maxAgeMs: Long = 86400000L) { // 24 hours default
        val now = Clock.System.now().toEpochMilliseconds()
        val expiredKeys = cache.filter { (_, entry) ->
            now - entry.timestamp > maxAgeMs
        }.keys
        
        expiredKeys.forEach { key ->
            remove(key)
        }
    }

    fun evictLRU(targetSize: Long) {
        if (getCacheSize() <= targetSize) return
        
        val sortedByAccess = metadata.toList().sortedBy { it.second.lastAccessed }
        var currentSize = getCacheSize()
        
        for ((key, _) in sortedByAccess) {
            if (currentSize <= targetSize) break
            val fileSize = metadata[key]?.size ?: 0
            remove(key)
            currentSize -= fileSize
        }
    }
}
