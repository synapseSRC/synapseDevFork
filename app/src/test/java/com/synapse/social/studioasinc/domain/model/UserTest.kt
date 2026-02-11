package com.synapse.social.studioasinc.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {

    @Test
    fun `toUser correctly parses valid map`() {
        val map = hashMapOf<String, Any?>(
            "id" to "123",
            "uid" to "user123",
            "email" to "test@example.com",
            "username" to "testuser",
            "status" to "online"
        )
        val user = map.toUser()
        assertEquals("123", user.id)
        assertEquals("user123", user.uid)
        assertEquals("test@example.com", user.email)
        assertEquals("testuser", user.username)
        assertEquals(UserStatus.ONLINE, user.status)
    }

    @Test
    fun `toUser handles missing uid by defaulting to empty string`() {
        val map = hashMapOf<String, Any?>()
        val user = map.toUser()
        assertEquals("", user.uid)
    }

    @Test
    fun `toUser handles status variations`() {
        assertEquals(UserStatus.ONLINE, hashMapOf<String, Any?>("status" to "online").toUser().status)
        assertEquals(UserStatus.ONLINE, hashMapOf<String, Any?>("status" to "ONLINE").toUser().status)
        assertEquals(UserStatus.OFFLINE, hashMapOf<String, Any?>("status" to "offline").toUser().status)
        assertEquals(UserStatus.OFFLINE, hashMapOf<String, Any?>("status" to "invalid").toUser().status)
        assertEquals(UserStatus.OFFLINE, hashMapOf<String, Any?>("status" to null).toUser().status)
    }
}
