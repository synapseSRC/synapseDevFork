
package com.synapse.social.studioasinc.shared.data

import java.io.File

actual class FileUploader actual constructor() {
    actual fun readFile(path: String): ByteArray {
        val file = File(path)
        return file.readBytes()
    }
}
