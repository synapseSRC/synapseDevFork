package com.synapse.social.studioasinc.shared.data

actual object PlatformUtils {
    actual fun sha1(input: String): String {
        return "" // Not implemented or use CryptoKit
    }

    actual fun sha256(input: String): String {
        return ""
    }

    actual fun hmacSha256(key: ByteArray, data: String): ByteArray {
        return ByteArray(0)
    }
}
