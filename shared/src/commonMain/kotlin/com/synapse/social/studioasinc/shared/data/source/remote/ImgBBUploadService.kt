package com.synapse.social.studioasinc.shared.data.source.remote

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.onUpload
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject

class ImgBBUploadService(private val client: HttpClient) : UploadService {
    override suspend fun upload(
        fileBytes: ByteArray,
        fileName: String,
        config: StorageConfig,
        bucketName: String?,
        onProgress: (Float) -> Unit
    ): String {
        val apiKey = config.imgBBKey

        val response: JsonObject = client.post("https://api.imgbb.com/1/upload") {
            url {
                parameters.append("key", apiKey)
            }
            setBody(MultiPartFormDataContent(formData {
                val fileHeaders = Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                }
                append(FormPart("image", fileBytes, fileHeaders))
            }))

            onUpload { bytesSentTotal, totalBytes ->
                if (totalBytes != null && totalBytes > 0) {
                    onProgress(bytesSentTotal.toFloat() / totalBytes.toFloat())
                }
            }
        }.body()

        val data = response["data"]?.jsonObject ?: throw Exception("ImgBB upload failed")
        return data["url"]?.jsonPrimitive?.content ?: throw Exception("ImgBB URL missing")
    }
}
