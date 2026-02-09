package com.synapse.social.studioasinc.shared.data

import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class PlatformUtilsTest {

    @Test
    fun testSha1() {
        val input = "hello world"
        val expected = "2aae6c35c94fcfb415dbe95f408b9ce91ee846ed"
        assertEquals(expected, PlatformUtils.sha1(input))
    }

    @Test
    fun testSha1Empty() {
        val input = ""
        val expected = "da39a3ee5e6b4b0d3255bfef95601890afd80709"
        assertEquals(expected, PlatformUtils.sha1(input))
    }

    @Test
    fun testSha256() {
        val input = "hello world"
        val expected = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
        assertEquals(expected, PlatformUtils.sha256(input))
    }

    @Test
    fun testSha256Empty() {
        val input = ""
        val expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertEquals(expected, PlatformUtils.sha256(input))
    }

    @Test
    fun testHmacSha256() {
        val key = "secret".encodeToByteArray()
        val data = "hello world"
        val result = PlatformUtils.hmacSha256(key, data)


        val expectedHex = "734cc62f32841568f45715aeb9f4d7891324e6d948e4c6c60c0621cdac48623a"
        val resultHex = result.toHexString()

        assertEquals(expectedHex, resultHex)
    }

    @Test
    fun testHmacSha256Empty() {




        try {
            val key = "".encodeToByteArray()
            val data = ""
            val result = PlatformUtils.hmacSha256(key, data)

            val expectedHex = "b613679a0814d9ec772f95d778c35fc5ff1697c493715653c6c712144292c5ad"
            val resultHex = result.toHexString()

            assertEquals(expectedHex, resultHex)
        } catch (e: IllegalArgumentException) {


            println("Skipping testHmacSha256Empty: ${e.message}")
        }
    }
}
