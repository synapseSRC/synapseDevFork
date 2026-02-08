package com.synapse.social.studioasinc.shared.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

actual class FileUploader(private val context: Context) {
    actual fun readFile(path: String): ByteArray {
        return if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to open input stream for $path")
        } else {
            val file = File(path)
            file.readBytes()
        }
    }

    actual fun getFileName(path: String): String {
        if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            var result: String? = null
            if (uri.scheme == "content") {
                 try {
                     context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                         if (cursor.moveToFirst()) {
                             val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                             if (index != -1) {
                                 result = cursor.getString(index)
                             }
                         }
                     }
                 } catch (e: Exception) {

                 }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != null && cut != -1) {
                    result = result?.substring(cut + 1)
                }
            }
            return result ?: "upload_file"
        } else {
            return File(path).name
        }
    }
}
