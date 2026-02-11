package com.synapse.social.studioasinc.shared.data.auth

import com.synapse.social.studioasinc.shared.data.local.SecureStorage
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.junit.Assert.*

class SupabaseAuthenticationServiceTest {

    private val mockClient = mock(SupabaseClient::class.java)
    private val fakeSecureStorage = FakeSecureStorage()

    @Test
    fun testStoreAndClearTokens() = runTest {
        // Prepare data
        val accessToken = "access_token_123"
        val refreshToken = "refresh_token_123"
        val userId = "user_123"
        val userEmail = "test@example.com"
        val expiresIn = 3600

        val service = SupabaseAuthenticationService(
            client = mockClient,
            secureStorage = fakeSecureStorage
        )

        service.storeSessionTokens(accessToken, refreshToken, userId, userEmail, expiresIn)

        assertEquals(accessToken, fakeSecureStorage.getString("auth_access_token"))
        assertEquals(refreshToken, fakeSecureStorage.getString("auth_refresh_token"))
        assertEquals(userId, fakeSecureStorage.getString("auth_user_id"))
        assertEquals(userEmail, fakeSecureStorage.getString("auth_user_email"))
        assertEquals(expiresIn.toString(), fakeSecureStorage.getString("auth_expires_in"))

        service.clearStoredTokens()

        assertNull(fakeSecureStorage.getString("auth_access_token"))
        assertNull(fakeSecureStorage.getString("auth_refresh_token"))
        assertNull(fakeSecureStorage.getString("auth_user_id"))
        assertNull(fakeSecureStorage.getString("auth_user_email"))
        assertNull(fakeSecureStorage.getString("auth_expires_in"))
    }
}

class FakeSecureStorage : SecureStorage {
    private val storage = mutableMapOf<String, String>()

    override fun save(key: String, value: String) {
        storage[key] = value
    }

    override fun getString(key: String): String? {
        return storage[key]
    }

    override fun clear(key: String) {
        storage.remove(key)
    }
}
