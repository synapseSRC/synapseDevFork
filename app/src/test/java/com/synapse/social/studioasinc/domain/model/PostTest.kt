package com.synapse.social.studioasinc.shared.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PostTest {

    @Test
    fun `toPost converts basic fields correctly`() {
        val map = HashMap<String, Any>()
        map["id"] = "post123"
        map["post_text"] = "Hello World"
        map["likes_count"] = 10
        map["author_is_verified"] = true

        val post = map.toPost()

        assertEquals("post123", post.id)
        assertEquals("Hello World", post.postText)
        assertEquals(10, post.likesCount)
        assertEquals(true, post.isVerified)
    }

    @Test
    fun `toPost handles nested metadata and user correctly`() {
        val userMap = HashMap<String, Any>()
        userMap["uid"] = "user1"
        userMap["status"] = "online" // Lowercase

        val metadataMap = HashMap<String, Any>()
        metadataMap["tagged_people"] = listOf(userMap)

        val feelingMap = HashMap<String, Any>()
        feelingMap["emoji"] = "😊"
        feelingMap["text"] = "Happy"
        feelingMap["type"] = "MOOD"
        metadataMap["feeling"] = feelingMap

        val map = HashMap<String, Any>()
        map["metadata"] = metadataMap

        val post = map.toPost()

        assertNotNull(post.metadata)
        assertEquals(1, post.metadata?.taggedPeople?.size)
        assertEquals("user1", post.metadata?.taggedPeople?.get(0)?.uid)
        assertEquals(UserStatus.ONLINE, post.metadata?.taggedPeople?.get(0)?.status)

        assertEquals(FeelingType.MOOD, post.metadata?.feeling?.type)
    }

    @Test
    fun `toPost handles invalid enum values gracefully`() {
        // UserStatus fallback
        val userMap = HashMap<String, Any>()
        userMap["uid"] = "user2"
        userMap["status"] = "busy" // Invalid

        val metadataMap = HashMap<String, Any>()
        metadataMap["tagged_people"] = listOf(userMap)

        // FeelingType fallback
        val feelingMap = HashMap<String, Any>()
        feelingMap["emoji"] = "😡"
        feelingMap["text"] = "Angry"
        feelingMap["type"] = "UNKNOWN_TYPE" // Invalid
        metadataMap["feeling"] = feelingMap

        val map = HashMap<String, Any>()
        map["metadata"] = metadataMap

        val post = map.toPost()

        assertEquals(UserStatus.OFFLINE, post.metadata?.taggedPeople?.get(0)?.status)
        assertEquals(FeelingType.MOOD, post.metadata?.feeling?.type)
    }

    @Test
    fun `toPost handles mixed types for numbers`() {
        val map = HashMap<String, Any>()
        map["likes_count"] = 5.0 // Double instead of Int
        map["timestamp"] = 1234567890.0 // Double instead of Long

        val post = map.toPost()

        assertEquals(5, post.likesCount)
        assertEquals(1234567890L, post.timestamp)
    }
}
