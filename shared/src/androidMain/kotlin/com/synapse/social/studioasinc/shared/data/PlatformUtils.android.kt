
package com.synapse.social.studioasinc.shared.data
import java.security.MessageDigest

actual object PlatformUtils {
    actual fun sha1(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
