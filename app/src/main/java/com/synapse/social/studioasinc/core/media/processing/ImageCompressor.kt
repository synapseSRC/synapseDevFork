package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.core.media.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * ImageCompressor handles image compression with size and quality optimization.
 * Maintains aspect ratio while reducing file size to meet target requirements.
 */
class ImageCompressor @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val MAX_WIDTH = 1920
        private const val MAX_HEIGHT = 1080
        private const val MAX_FILE_SIZE_MB = 2
        private const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024L // 2MB
        private const val DEFAULT_COMPRESSION_QUALITY = 85
        private const val MIN_COMPRESSION_QUALITY = 50
    }

    /**
     * Compresses an image from a File.
     *
     * @param file The image file to compress
     * @return Result containing the compressed image file or error
     */
    suspend fun compressFile(file: File): Result<File> {
        return compress(Uri.fromFile(file), MAX_FILE_SIZE_BYTES)
    }

    /**
     * Compresses an image from URI to meet default size requirements.
     *
     * @param uri The URI of the image to compress
     * @return Result containing the compressed image file or error
     */
    suspend fun compress(uri: Uri): Result<File> {
        return compress(uri, MAX_FILE_SIZE_BYTES)
    }

    /**
     * Compresses an image from URI to meet specific size requirements.
     *
     * @param uri The URI of the image to compress
     * @param maxSizeBytes Maximum file size in bytes
     * @return Result containing the compressed image file or error
     */
    suspend fun compress(uri: Uri, maxSizeBytes: Long): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Decode the image
            val decodedBitmap = decodeImage(uri, MAX_WIDTH, MAX_HEIGHT)
                ?: return@withContext Result.failure(IOException("Failed to decode bitmap from $uri"))

            // Apply EXIF orientation
            val orientedBitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                try {
                    applyExifOrientation(uri, decodedBitmap)
                } catch (e: Exception) {
                    // If EXIF processing fails, use original bitmap
                    decodedBitmap
                }
            } else {
                decodedBitmap
            }

            // Scale bitmap to target dimensions if needed
            val scaledBitmap = scaleToTargetSize(orientedBitmap, MAX_WIDTH, MAX_HEIGHT)

            // Clean up intermediate bitmap if different
            if (scaledBitmap != orientedBitmap && scaledBitmap != decodedBitmap) {
                orientedBitmap.recycle()
            }
            if (orientedBitmap != decodedBitmap) {
                decodedBitmap.recycle()
            }

            // Validate bitmap safety before compression
            if (!isBitmapSafeToProcess(scaledBitmap)) {
                scaledBitmap.recycle()
                return@withContext Result.failure(IOException("Bitmap too large to process safely"))
            }

            // Compress iteratively to meet file size target
            val compressedFile = compressIteratively(scaledBitmap, maxSizeBytes)

            // Clean up bitmap
            scaledBitmap.recycle()

            Result.success(compressedFile)

        } catch (e: OutOfMemoryError) {
            Result.failure(IOException("Out of memory while compressing image", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Legacy method kept for compatibility but redirected to main compress method
    suspend fun compressToSize(uri: Uri, maxSizeBytes: Long): Result<File> {
        return compress(uri, maxSizeBytes)
    }

    /**
     * Decodes an image from a URI, handling API level differences and large images.
     */
    private fun decodeImage(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeImageApi28(uri, reqWidth, reqHeight)
        } else {
            decodeImageLegacy(uri, reqWidth, reqHeight)
        }
    }

    /**
     * Decodes image using ImageDecoder (API 28+).
     */
    private fun decodeImageApi28(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                val size = info.size
                var sampleSize = 1
                if (size.width > reqWidth || size.height > reqHeight) {
                    val halfWidth = size.width / 2
                    val halfHeight = size.height / 2
                    while ((halfWidth / sampleSize) >= reqWidth && (halfHeight / sampleSize) >= reqHeight) {
                        sampleSize *= 2
                    }
                }

                // For very large images (>4k), ensure we don't start with too little downsampling
                var effectiveWidth = size.width / sampleSize
                var effectiveHeight = size.height / sampleSize

                while (effectiveWidth > 4096 || effectiveHeight > 4096) {
                     sampleSize *= 2
                     effectiveWidth = size.width / sampleSize
                     effectiveHeight = size.height / sampleSize
                }

                decoder.setTargetSampleSize(sampleSize)

                // Check if image has transparency to decide config
                val mimeType = info.mimeType
                if (mimeType == "image/png" || mimeType == "image/webp") {
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                } else {
                    decoder.memorySizePolicy = ImageDecoder.MEMORY_POLICY_LOW_RAM
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }

                decoder.isMutableRequired = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes image using BitmapFactory (Legacy).
     */
    private fun decodeImageLegacy(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            var sampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Retry logic for OOM
            while (true) {
                val newInputStream = context.contentResolver.openInputStream(uri)
                    ?: return null

                try {
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        // Check mime type for transparency
                        val mimeType = options.outMimeType
                        if (mimeType == "image/png" || mimeType == "image/webp") {
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        } else {
                            inPreferredConfig = Bitmap.Config.RGB_565
                        }
                    }

                    val bitmap = BitmapFactory.decodeStream(newInputStream, null, decodeOptions)
                    newInputStream.close()
                    return bitmap
                } catch (e: OutOfMemoryError) {
                    newInputStream.close()
                    sampleSize *= 2
                    if (sampleSize > 64) { // Safety break
                        return null
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        } finally {
            inputStream?.close()
        }
    }

    /**
     * Calculates the optimal inSampleSize for efficient bitmap decoding.
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        var effectiveWidth = width / inSampleSize
        var effectiveHeight = height / inSampleSize

        while (effectiveWidth > 4096 || effectiveHeight > 4096) {
             inSampleSize *= 2
             effectiveWidth = width / inSampleSize
             effectiveHeight = height / inSampleSize
        }

        return inSampleSize
    }

    /**
     * Applies EXIF orientation to the bitmap.
     */
    private suspend fun applyExifOrientation(uri: Uri, bitmap: Bitmap): Bitmap = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext bitmap

            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }
                else -> return@withContext bitmap
            }

            try {
                val orientedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (orientedBitmap != bitmap) {
                    bitmap.recycle()
                }
                orientedBitmap
            } catch (e: OutOfMemoryError) {
                bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    /**
     * Scales bitmap to fit within target dimensions.
     */
    private fun scaleToTargetSize(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxWidth
            targetHeight = (maxWidth / aspectRatio).toInt()
        } else {
            targetHeight = maxHeight
            targetWidth = (maxHeight * aspectRatio).toInt()
        }

        return try {
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            scaledBitmap
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }

    /**
     * Compresses bitmap iteratively to meet target file size.
     */
    private suspend fun compressIteratively(bitmap: Bitmap, targetSizeBytes: Long): File = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("compressed_image_", ".jpg", context.cacheDir)

        var minQuality = MIN_COMPRESSION_QUALITY
        var maxQuality = 95
        var bestQuality = minQuality
        var bestData: ByteArray? = null

        // Check if compression needed at max quality
        val initialStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, maxQuality, initialStream)
        val initialData = initialStream.toByteArray()
        initialStream.close()

        if (initialData.size.toLong() <= targetSizeBytes) {
            bestData = initialData
        } else {
            // Binary search for the best quality
            var attempts = 0
            val maxAttempts = 10

            while (minQuality <= maxQuality && attempts < maxAttempts) {
                val midQuality = (minQuality + maxQuality) / 2
                val outputStream = ByteArrayOutputStream()

                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, midQuality, outputStream)
                    val compressedData = outputStream.toByteArray()
                    val currentSize = compressedData.size.toLong()

                    if (currentSize <= targetSizeBytes) {
                        bestQuality = midQuality
                        bestData = compressedData
                        minQuality = midQuality + 1
                    } else {
                        maxQuality = midQuality - 1
                    }
                } catch (e: OutOfMemoryError) {
                    maxQuality = midQuality - 1
                } finally {
                    outputStream.close()
                }
                attempts++
            }
        }

        val finalData = bestData ?: run {
            val fallbackStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, MIN_COMPRESSION_QUALITY, fallbackStream)
            val data = fallbackStream.toByteArray()
            fallbackStream.close()
            data
        }

        try {
            FileOutputStream(tempFile).use { fileOutputStream ->
                fileOutputStream.write(finalData)
            }
        } catch (e: IOException) {
            tempFile.delete()
            throw e
        }

        tempFile
    }

    /**
     * Validates if the bitmap can be safely processed without causing OOM.
     */
    private fun isBitmapSafeToProcess(bitmap: Bitmap): Boolean {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val availableMemory = maxMemory - usedMemory

        val bitmapMemory = bitmap.byteCount * 3L

        return bitmapMemory < availableMemory * 0.5
    }
}
