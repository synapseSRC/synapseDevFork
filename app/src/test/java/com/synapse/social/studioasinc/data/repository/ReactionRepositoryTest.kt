package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.ReactionType
import io.github.jan.supabase.SupabaseClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class ReactionRepositoryTest {

    @Test
    fun `applyReactionSummaries correctly maps summaries to posts`() {
        // Arrange
        val mockClient = Mockito.mock(SupabaseClient::class.java)
        val repository = ReactionRepository(mockClient)

        val post1 = Post(id = "post1")
        val post2 = Post(id = "post2")
        val posts = listOf(post1, post2)

        val summaries = listOf(
            ReactionRepository.PostReactionSummary(
                postId = "post1",
                reactionCounts = mapOf("LIKE" to 5, "LOVE" to 3),
                userReaction = "LOVE"
            ),
            ReactionRepository.PostReactionSummary(
                postId = "post2",
                reactionCounts = mapOf("HAHA" to 2),
                userReaction = null
            )
        )

        // Act
        val result = repository.applyReactionSummaries(posts, summaries)

        // Assert
        val resultPost1 = result.find { it.id == "post1" }!!
        assertEquals(8, resultPost1.likesCount)
        assertEquals(ReactionType.LOVE, resultPost1.userReaction)
        assertEquals(5, resultPost1.reactions?.get(ReactionType.LIKE))
        assertEquals(3, resultPost1.reactions?.get(ReactionType.LOVE))

        val resultPost2 = result.find { it.id == "post2" }!!
        assertEquals(2, resultPost2.likesCount)
        assertNull(resultPost2.userReaction)
        assertEquals(2, resultPost2.reactions?.get(ReactionType.HAHA))
    }

    @Test
    fun `applyReactionSummaries handles empty summaries`() {
        val mockClient = Mockito.mock(SupabaseClient::class.java)
        val repository = ReactionRepository(mockClient)
        val post1 = Post(id = "post1")
        val posts = listOf(post1)
        val summaries = emptyList<ReactionRepository.PostReactionSummary>()

        // Act
        val result = repository.applyReactionSummaries(posts, summaries)

        // Assert
        val resultPost1 = result.first()
        assertEquals(0, resultPost1.likesCount)
        assertNull(resultPost1.userReaction)
        assertTrue(resultPost1.reactions?.isEmpty() == true)
    }

    @Test
    fun `applyReactionSummaries handles case insensitivity and unknown types`() {
        val mockClient = Mockito.mock(SupabaseClient::class.java)
        val repository = ReactionRepository(mockClient)
        val post1 = Post(id = "post1")
        val posts = listOf(post1)

        val summaries = listOf(
            ReactionRepository.PostReactionSummary(
                postId = "post1",
                reactionCounts = mapOf("like" to 2, "LoVe" to 3, "unknown_type" to 1),
                userReaction = "haHA"
            )
        )

        // Act
        val result = repository.applyReactionSummaries(posts, summaries)

        // Assert
        val resultPost1 = result.first()
        // Unknown type defaults to LIKE
        // "unknown_type" -> LIKE
        // "like" -> LIKE
        // Total LIKE = 2 + 1 = 3

        assertEquals(6, resultPost1.likesCount) // 2+3+1
        assertEquals(3, resultPost1.reactions?.get(ReactionType.LIKE))
        assertEquals(3, resultPost1.reactions?.get(ReactionType.LOVE))
        assertEquals(ReactionType.HAHA, resultPost1.userReaction)
    }
}
