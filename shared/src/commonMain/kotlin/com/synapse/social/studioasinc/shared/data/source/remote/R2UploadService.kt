package com.synapse.social.studioasinc.shared.data.source.remote

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.plugins.onUpload
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class R2UploadService(private val client: HttpClient) : UploadService {
    override suspend fun upload(
        fileBytes: ByteArray,
        fileName: String,
        config: StorageConfig,
        bucketName: String?,
        onProgress: (Float) -> Unit
    ): String {
        val accountId = config.r2AccountId
        val accessKeyId = config.r2AccessKeyId
        val secretAccessKey = config.r2SecretAccessKey
        val targetBucket = bucketName ?: config.r2BucketName

        if (accountId.isBlank() || accessKeyId.isBlank() || secretAccessKey.isBlank() || targetBucket.isBlank()) {
            throw Exception("R2 configuration is incomplete")
        }


        val encodedFileName = fileName.encodeURLPathPart()

        val host = "$accountId.r2.cloudflarestorage.com"
        val path = "/$targetBucket/$encodedFileName"
        val url = "https://$host$path"

        val amzDate = getAmzDate()

        val headers = S3Signer.signS3(
            method = "PUT",
            canonicalPath = path,
            region = "auto",
            host = host,
            accessKeyId = accessKeyId,
            secretAccessKey = secretAccessKey,
            amzDate = amzDate,
            contentType = "application/octet-stream"
        )

        try {
            val response = client.put(url) {
                headers.forEach { (k, v) ->
                    this.headers.append(k, v)
                }
                setBody(fileBytes)
                onUpload { bytesSentTotal, totalBytes ->
                     if (totalBytes != null && totalBytes > 0) {
                        onProgress(bytesSentTotal.toFloat() / totalBytes.toFloat())
                    }
                }
            }

            if (!response.status.isSuccess()) {
                throw Exception("Upload failed with status: ${response.status}")
            }

            return url
        } catch (e: Exception) {
            throw Exception("R2 upload failed: ${e.message}")
        }
    }

    private fun getAmzDate(): String {
        val now = Clock.System.now()
        val dateTime = now.toLocalDateTime(TimeZone.UTC)
        val year = dateTime.year.toString().padStart(4, '0')
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        val second = dateTime.second.toString().padStart(2, '0')
        return "${year}${month}${day}T${hour}${minute}${second}Z"
    }
}
