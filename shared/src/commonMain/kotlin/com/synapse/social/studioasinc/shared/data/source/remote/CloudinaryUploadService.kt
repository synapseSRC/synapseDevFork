package com.synapse.social.studioasinc.shared.data.source.remote

import com.synapse.social.studioasinc.shared.data.PlatformUtils
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.UploadError
import com.synapse.social.studioasinc.shared.util.TimeProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject

class CloudinaryUploadService(private val client: HttpClient) : UploadService {
    override suspend fun upload(
        fileBytes: ByteArray,
        fileName: String,
        config: StorageConfig,
        bucketName: String?,
        onProgress: (Float) -> Unit
    ): String {
        val cloudName = config.cloudinaryCloudName
        val apiKey = config.cloudinaryApiKey
        val apiSecret = config.cloudinaryApiSecret

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw Exception("Cloudinary configuration is incomplete")
        }

        val timestamp = (TimeProvider.nowMillis() / 1000).toString()
        val toSign = "timestamp=$timestamp$apiSecret"
        val signature = PlatformUtils.sha1(toSign)

        return try {
            val response: JsonObject = client.post("https://api.cloudinary.com/v1_1/$cloudName/auto/upload") {
                setBody(MultiPartFormDataContent(formData {
                    val fileHeaders = Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    }
                    append(FormPart("file", fileBytes, fileHeaders))
                    append("api_key", apiKey)
                    append("timestamp", timestamp)
                    append("signature", signature)
                }))

                onUpload { bytesSentTotal, totalBytes ->
                    if (totalBytes != null && totalBytes > 0) {
                        onProgress(bytesSentTotal.toFloat() / totalBytes.toFloat())
                    }
                }
            }.body()

            response["secure_url"]?.jsonPrimitive?.content
                ?: throw Exception(extractCloudinaryError(response))
        } catch (e: Exception) {
            throw Exception("Cloudinary upload failed: ${e.message ?: "Unknown error"}")
        }
    }

    private fun extractCloudinaryError(response: JsonObject): String {
        val errorMessage = response["error"]
            ?.jsonObject
            ?.get("message")
            ?.jsonPrimitive
            ?.content

        return errorMessage ?: "Cloudinary URL missing in upload response"
    }
}
