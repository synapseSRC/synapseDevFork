package com.synapse.social.studioasinc.shared.data

import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.NSString
import platform.Foundation.lastPathComponent
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

actual class FileUploader {
    actual fun readFile(path: String): ByteArray {
         val data = NSData.dataWithContentsOfFile(path)
         if (data == null) return ByteArray(0)

         val byteArray = ByteArray(data.length.toInt())
         byteArray.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
         }
         return byteArray
    }

    actual fun getFileName(path: String): String {
        return (path as NSString).lastPathComponent
    }
}
