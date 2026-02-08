package com.synapse.social.studioasinc.shared.data

expect class FileUploader {
    fun readFile(path: String): ByteArray
    fun getFileName(path: String): String
}
