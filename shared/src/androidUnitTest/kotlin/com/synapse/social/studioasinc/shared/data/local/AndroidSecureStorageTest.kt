package com.synapse.social.studioasinc.shared.data.local

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AndroidSecureStorageTest {

    @Test
    fun testSaveAndGetString() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        `when`(prefs.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.commit()).thenReturn(true)
        `when`(prefs.getString("test_key", null)).thenReturn("test_value")

        val storage = AndroidSecureStorage(context, prefs)

        storage.save("test_key", "test_value")
        verify(editor).putString("test_key", "test_value")
        verify(editor).commit()

        val result = storage.getString("test_key")
        assertEquals("test_value", result)
    }

    @Test
    fun testClear() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        `when`(prefs.edit()).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        `when`(editor.commit()).thenReturn(true)

        val storage = AndroidSecureStorage(context, prefs)

        storage.clear("test_key")
        verify(editor).remove("test_key")
        verify(editor).commit()
    }
}
