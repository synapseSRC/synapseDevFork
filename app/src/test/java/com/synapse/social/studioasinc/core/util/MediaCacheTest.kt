package com.synapse.social.studioasinc.core.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MediaCacheTest {

    private lateinit var context: Context
    private lateinit var mediaCache: MediaCache
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mediaCache = MediaCache(context)

        // Create a temporary file to use for caching
        tempFile = File.createTempFile("test_image", ".jpg", context.cacheDir)
        tempFile.writeText("test content")
    }

    @After
    fun tearDown() = runBlocking {
        mediaCache.clear()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    @Test
    fun testPutAndGet() = runTest {
        val key = "test_key"
        mediaCache.put(key, tempFile)

        val cachedFile = mediaCache.get(key)
        assertNotNull(cachedFile)
        assertTrue(cachedFile!!.exists())
        assertEquals(tempFile.length(), cachedFile.length())
    }

    @Test
    fun testRemove() = runTest {
        val key = "test_key_remove"
        mediaCache.put(key, tempFile)

        assertNotNull(mediaCache.get(key))

        mediaCache.remove(key)
        assertNull(mediaCache.get(key))
    }
}
