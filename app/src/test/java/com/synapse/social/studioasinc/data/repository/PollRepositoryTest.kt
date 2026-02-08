package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.domain.model.PollOption
import com.synapse.social.studioasinc.domain.model.PollOptionResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.plugins.PluginManager
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.PostgrestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestResult
import io.github.jan.supabase.postgrest.query.PostgrestFilterBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class PollRepositoryTest {

    @Mock lateinit var supabaseClient: SupabaseClient
    @Mock lateinit var pluginManager: PluginManager
    @Mock lateinit var auth: Auth
    @Mock lateinit var postgrest: Postgrest

    @Mock lateinit var postsBuilder: PostgrestBuilder
    @Mock lateinit var votesBuilder: PostgrestBuilder

    @Mock lateinit var userInfo: UserInfo

    private lateinit var repository: PollRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(supabaseClient.pluginManager).thenReturn(pluginManager)

        whenever(pluginManager.getPlugin(Auth)).thenReturn(auth)
        whenever(auth.currentUserOrNull()).thenReturn(userInfo)
        whenever(userInfo.id).thenReturn("test_user_id")

        whenever(pluginManager.getPlugin(Postgrest)).thenReturn(postgrest)

        whenever(postgrest["posts"]).thenReturn(postsBuilder)
        whenever(postgrest["poll_votes"]).thenReturn(votesBuilder)

        val dummyResult = mock<PostgrestResult>()
        whenever(dummyResult.data).thenReturn("[]")

        // Mocking Supabase operations
        // select: signature often varies, but usually 4 args: columns, head, count, request
        // Using any() for all arguments to be safe against signature variations
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(dummyResult)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(dummyResult)

        // insert: value, request
        whenever(votesBuilder.insert(any(), any())).thenReturn(dummyResult)

        // update: updateBlock, requestBlock
        whenever(votesBuilder.update(any(), any())).thenReturn(dummyResult)

        // delete: requestBlock
        whenever(votesBuilder.delete(any())).thenReturn(dummyResult)

        repository = PollRepository(supabaseClient)
    }

    @Test
    fun `getUserVote returns option index when vote exists`() = runTest {
        val jsonResponse = """[{"id":"vote1","post_id":"post1","user_id":"test_user_id","option_index":1}]"""
        val resultMock = mock<PostgrestResult>()
        whenever(resultMock.data).thenReturn(jsonResponse)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(resultMock)

        val result = repository.getUserVote("post1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
    }

    @Test
    fun `getUserVote returns null when no vote exists`() = runTest {
        val jsonResponse = "[]"
        val resultMock = mock<PostgrestResult>()
        whenever(resultMock.data).thenReturn(jsonResponse)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(resultMock)

        val result = repository.getUserVote("post1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `getUserVote returns failure on error`() = runTest {
        whenever(votesBuilder.select(any(), any(), any(), any())).thenThrow(RuntimeException("Network error"))

        val result = repository.getUserVote("post1")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `submitVote inserts new vote when no previous vote exists`() = runTest {
        val futureTime = Instant.now().plus(1, ChronoUnit.HOURS).toString()
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"}],"poll_end_time":""}"""
        val noVoteJson = "[]"

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        val voteResult = mock<PostgrestResult>()
        whenever(voteResult.data).thenReturn(noVoteJson)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(voteResult)

        val result = repository.submitVote("post1", 1)

        assertTrue(result.isSuccess)
        verify(votesBuilder).insert(any(), any())
    }

    @Test
    fun `submitVote updates vote when previous vote exists`() = runTest {
        val futureTime = Instant.now().plus(1, ChronoUnit.HOURS).toString()
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"}],"poll_end_time":""}"""
        val existingVoteJson = """[{"id":"vote1","post_id":"post1","user_id":"test_user_id","option_index":0}]"""

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        val voteResult = mock<PostgrestResult>()
        whenever(voteResult.data).thenReturn(existingVoteJson)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(voteResult)

        val result = repository.submitVote("post1", 1)

        assertTrue(result.isSuccess)
        verify(votesBuilder).update(any(), any())
    }

    @Test
    fun `submitVote fails when poll has ended`() = runTest {
        val pastTime = Instant.now().minus(1, ChronoUnit.HOURS).toString()
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"}],"poll_end_time":""}"""

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        val result = repository.submitVote("post1", 1)

        assertTrue(result.isFailure)
        assertEquals("Poll has ended", result.exceptionOrNull()?.message)
    }

    @Test
    fun `submitVote fails when option index is invalid`() = runTest {
        val futureTime = Instant.now().plus(1, ChronoUnit.HOURS).toString()
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"}],"poll_end_time":""}"""

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        // Invalid index: -1
        val resultNegative = repository.submitVote("post1", -1)
        assertTrue(resultNegative.isFailure)
        assertEquals("Invalid option index", resultNegative.exceptionOrNull()?.message)

        // Invalid index: 2 (size is 2, so 0 and 1 are valid)
        val resultOverflow = repository.submitVote("post1", 2)
        assertTrue(resultOverflow.isFailure)
        assertEquals("Invalid option index", resultOverflow.exceptionOrNull()?.message)
    }

    @Test
    fun `revokeVote calls delete`() = runTest {
        val result = repository.revokeVote("post1")

        assertTrue(result.isSuccess)
        verify(votesBuilder).delete(any())
    }

    @Test
    fun `getPollResults calculates counts and percentages correctly`() = runTest {
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"},{"text":"Option 3"}],"poll_end_time":null}"""
        val votesJson = """
            [
                {"id":"v1","post_id":"post1","user_id":"u1","option_index":0},
                {"id":"v2","post_id":"post1","user_id":"u2","option_index":0},
                {"id":"v3","post_id":"post1","user_id":"u3","option_index":1}
            ]
        """

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        val votesResult = mock<PostgrestResult>()
        whenever(votesResult.data).thenReturn(votesJson)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(votesResult)

        val result = repository.getPollResults("post1")

        assertTrue(result.isSuccess)
        val options = result.getOrThrow()
        assertEquals(3, options.size)

        assertEquals("Option 1", options[0].text)
        assertEquals(2, options[0].voteCount)
        assertEquals(0.66f, options[0].votePercentage, 0.01f) // 2/3 = 0.666

        assertEquals("Option 2", options[1].text)
        assertEquals(1, options[1].voteCount)
        assertEquals(0.33f, options[1].votePercentage, 0.01f) // 1/3 = 0.333

        assertEquals("Option 3", options[2].text)
        assertEquals(0, options[2].voteCount)
        assertEquals(0.0f, options[2].votePercentage, 0.0f)
    }

    @Test
    fun `getBatchUserVotes returns map of post_id to option_index`() = runTest {
        val votesJson = """
            [
                {"id":"v1","post_id":"post1","user_id":"test_user_id","option_index":1},
                {"id":"v2","post_id":"post2","user_id":"test_user_id","option_index":0}
            ]
        """
        val resultMock = mock<PostgrestResult>()
        whenever(resultMock.data).thenReturn(votesJson)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(resultMock)

        val result = repository.getBatchUserVotes(listOf("post1", "post2"))

        assertTrue(result.isSuccess)
        val votesMap = result.getOrThrow()
        assertEquals(2, votesMap.size)
        assertEquals(1, votesMap["post1"])
        assertEquals(0, votesMap["post2"])
    }

    @Test
    fun `getBatchUserVotes returns empty map for empty input`() = runTest {
        val result = repository.getBatchUserVotes(emptyList())
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
        verify(votesBuilder, never()).select(any(), any(), any(), any())
    }

    @Test
    fun `getBatchPollVotes returns nested map of counts`() = runTest {
        val votesJson = """
            [
                {"id":"v1","post_id":"post1","user_id":"u1","option_index":0},
                {"id":"v2","post_id":"post1","user_id":"u2","option_index":0},
                {"id":"v3","post_id":"post1","user_id":"u3","option_index":1},
                {"id":"v4","post_id":"post2","user_id":"u4","option_index":2}
            ]
        """
        val resultMock = mock<PostgrestResult>()
        whenever(resultMock.data).thenReturn(votesJson)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(resultMock)

        val result = repository.getBatchPollVotes(listOf("post1", "post2"))

        assertTrue(result.isSuccess)
        val votesMap = result.getOrThrow()

        // Post 1
        val post1Votes = votesMap["post1"] ?: emptyMap()
        assertEquals(2, post1Votes[0])
        assertEquals(1, post1Votes[1])

        // Post 2
        val post2Votes = votesMap["post2"] ?: emptyMap()
        assertEquals(1, post2Votes[2])
    }
}
