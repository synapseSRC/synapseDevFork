
package com.synapse.social.studioasinc.shared.data

import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

actual class FileUploader actual constructor() {
    actual fun readFile(path: String): ByteArray {
         val data = NSData.dataWithContentsOfFile(path)
         if (data == null) return ByteArray(0)
         return ByteArray(data.length.toInt()).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
         }
    }

    private fun memcpy(dest: kotlinx.cinterop.CPointer<kotlinx.cinterop.ByteVar>, src: kotlinx.cinterop.CPointer<*>?, size: ULong) {
        platform.posix.memcpy(dest, src, size)
    }
}
