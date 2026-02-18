package com.synapse.social.studioasinc.shared.data.source.remote

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class S3SignerTest {

    @Test
    fun testSignS3Format() {
        val method = "GET"
        val canonicalPath = "/test"
        val region = "us-east-1"
        val host = "test.s3.amazonaws.com"
        val accessKeyId = "AKID"
        val secretAccessKey = "SECRET"
        val amzDate = "20230101T000000Z"
        val contentType = "application/json"

        val headers = S3Signer.signS3(
            method = method,
            canonicalPath = canonicalPath,
            region = region,
            host = host,
            accessKeyId = accessKeyId,
            secretAccessKey = secretAccessKey,
            amzDate = amzDate,
            contentType = contentType
        )

        // Check required headers
        assertTrue(headers.containsKey("Authorization"), "Authorization header missing")
        assertTrue(headers.containsKey("x-amz-date"), "x-amz-date header missing")
        assertTrue(headers.containsKey("x-amz-content-sha256"), "x-amz-content-sha256 header missing")
        assertTrue(headers.containsKey("Content-Type"), "Content-Type header missing")
        assertTrue(headers.containsKey("Host"), "Host header missing")

        // Verify values
        assertEquals(amzDate, headers["x-amz-date"])
        assertEquals("UNSIGNED-PAYLOAD", headers["x-amz-content-sha256"])
        assertEquals(contentType, headers["Content-Type"])
        assertEquals(host, headers["Host"])

        val authHeader = headers["Authorization"] ?: ""
        assertTrue(authHeader.startsWith("AWS4-HMAC-SHA256 Credential=AKID/20230101/us-east-1/s3/aws4_request"), "Authorization header format incorrect: $authHeader")
        assertTrue(authHeader.contains("SignedHeaders=content-type;host;x-amz-content-sha256;x-amz-date"), "SignedHeaders incorrect: $authHeader")
        assertTrue(authHeader.contains("Signature="), "Signature missing: $authHeader")

        // Ensure signature is hex string (implied by execution without error and non-empty result)
        val signature = authHeader.substringAfter("Signature=")
        assertTrue(signature.length == 64, "Signature should be 64 characters (SHA-256 hex string), but was ${signature.length}")
        assertTrue(signature.all { it.isDigit() || it in 'a'..'f' }, "Signature should be lowercase hex string: $signature")
    }
}
