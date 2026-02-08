package com.synapse.social.studioasinc.shared.data.crypto.store

import android.content.Context
import android.content.SharedPreferences
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.anyString

class AndroidSignalStoreTest {

    @Test
    fun testDeleteAllSessions_withNullName_removesAllSessionKeys() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        val keys = mapOf(
            "session_Alice_123" to "data",
            "session_Bob_456" to "data",
            "identity_Alice" to "data",
            "other_key" to "data"
        )

        `when`(prefs.all).thenReturn(keys)
        `when`(prefs.edit()).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        `when`(editor.commit()).thenReturn(true)

        val store = AndroidSignalStore(context, prefs)
        store.deleteAllSessions(null)

        verify(editor).remove("session_Alice_123")
        verify(editor).remove("session_Bob_456")
        verify(editor, never()).remove("identity_Alice")
        verify(editor, never()).remove("other_key")
        verify(editor).commit()
    }

    @Test
    fun testDeleteAllSessions_withName_removesOnlyMatchingSessionKeys() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        val keys = mapOf(
            "session_Alice_123" to "data",
            "session_Bob_456" to "data",
            "session_Alice_789" to "data",
            "identity_Alice" to "data"
        )

        `when`(prefs.all).thenReturn(keys)
        `when`(prefs.edit()).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        `when`(editor.commit()).thenReturn(true)

        val store = AndroidSignalStore(context, prefs)
        store.deleteAllSessions("Alice")

        verify(editor).remove("session_Alice_123")
        verify(editor).remove("session_Alice_789")
        verify(editor, never()).remove("session_Bob_456")
        verify(editor, never()).remove("identity_Alice")
        verify(editor).commit()
    }

    @Test
    fun testDeleteAllSessions_withUnderscoreInName_removesCorrectly() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        val keys = mapOf(
            "session_Alice_Bob_123" to "data",
            "session_Alice_Bob_456" to "data",
            "session_Alice_789" to "data"
        )

        `when`(prefs.all).thenReturn(keys)
        `when`(prefs.edit()).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        `when`(editor.commit()).thenReturn(true)

        val store = AndroidSignalStore(context, prefs)
        store.deleteAllSessions("Alice_Bob")

        verify(editor).remove("session_Alice_Bob_123")
        verify(editor).remove("session_Alice_Bob_456")
        verify(editor, never()).remove("session_Alice_789")
        verify(editor).commit()
    }
}
