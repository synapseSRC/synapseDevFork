package com.synapse.social.studioasinc.core.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileManagerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun validateAndCleanPath_stripsFilePrefix() {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val tempFile = File.createTempFile("test", ".jpg", cacheDir)

        val path = "file://${tempFile.absolutePath}"
        val result = FileManager.validateAndCleanPath(context, path)

        assertEquals(tempFile.absolutePath, result)
    }

    @Test
    fun validateAndCleanPath_stripsFilePrefix_caseInsensitive() {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val tempFile = File.createTempFile("test_case", ".jpg", cacheDir)

        val path = "FILE://${tempFile.absolutePath}"
        val result = FileManager.validateAndCleanPath(context, path)

        assertEquals(tempFile.absolutePath, result)
    }

    @Test
    fun validateAndCleanPath_handlesNoPrefix() {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val tempFile = File.createTempFile("test_noprefix", ".jpg", cacheDir)

        val path = tempFile.absolutePath
        val result = FileManager.validateAndCleanPath(context, path)

        assertEquals(tempFile.absolutePath, result)
    }

    @Test
    fun validateAndCleanPath_returnsNullForNonExistent() {
        val path = "file:///tmp/non_existent_file_12345.jpg"
        val result = FileManager.validateAndCleanPath(context, path)
        assertNull(result)
    }

    @Test
    fun validateAndCleanPath_returnsNullForPrivateDataDir() {
        val filesDir = context.filesDir
        if (!filesDir.exists()) filesDir.mkdirs()
        val privateFile = File(filesDir, "private.txt")
        privateFile.createNewFile()

        val path = privateFile.absolutePath
        val result = FileManager.validateAndCleanPath(context, path)

        assertNull("Should block access to private files dir", result)
    }
}
