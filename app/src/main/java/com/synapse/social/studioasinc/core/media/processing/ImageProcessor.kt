package com.synapse.social.studioasinc.core.media.processing

import android.content.Context
import android.net.Uri
import com.synapse.social.studioasinc.core.media.MediaConfig
import com.synapse.social.studioasinc.core.util.FileManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles image processing tasks such as compression and metadata extraction.
 */
@Singleton
class ImageProcessor @Inject constructor(
    private val context: Context,
    private val imageCompressor: ImageCompressor,
    private val config: MediaConfig
) {
    /**
     * Processes an image for upload.
     */
    suspend fun processImage(uri: Uri): Result<ProcessedMedia> {
        return try {
            val path = FileManager.getPathFromUri(context, uri)
                ?: return Result.failure(Exception("Could not retrieve file path from URI: $uri"))

            val originalFile = File(path)
            if (!originalFile.exists()) return Result.failure(Exception("File does not exist: $path"))

            val fileToUpload = if (config.compressImages) {
                val compressionResult = imageCompressor.compressFile(originalFile)
                compressionResult.getOrNull() ?: originalFile
            } else {
                originalFile
            }

            Result.success(ProcessedMedia(
                file = fileToUpload,
                mimeType = "image/jpeg",
                fileName = fileToUpload.name
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ProcessedMedia(
    val file: File,
    val mimeType: String,
    val fileName: String,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null
)
