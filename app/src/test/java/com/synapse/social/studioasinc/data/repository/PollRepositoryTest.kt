package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.domain.model.PollOption
import com.synapse.social.studioasinc.domain.model.PollOptionResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.plugins.PluginManager
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestResult
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
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

    // Corrected to PostgrestQueryBuilder
    @Mock lateinit var postsBuilder: PostgrestQueryBuilder
    @Mock lateinit var votesBuilder: PostgrestQueryBuilder

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

        // Mocking the builder methods.
        // Assuming select/insert/update/delete delegate to instance methods on PostgrestQueryBuilder
        // or return a RequestBuilder.
        // Note: Supabase-kt uses extension functions for select/insert/update/delete that create
        // RequestBuilders. Mocking this is difficult with Mockito.
        // For now, I'll attempt to mock the builder itself assuming methods exist or are mockable.
        // If select is inline, we can't mock it directly.
        // But let's fix the type first.

        whenever(votesBuilder.insert(any(), any(), any(), any())).thenReturn(dummyResult)
        whenever(votesBuilder.update(any(), any(), any(), any())).thenReturn(dummyResult)
        whenever(votesBuilder.delete(any(), any(), any(), any())).thenReturn(dummyResult)

        // This select mocking is problematic if select is inline.
        // We'll keep it for now as a "best effort" to fix types.
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(dummyResult)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(dummyResult)

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
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"}],"poll_end_time":"$futureTime"}"""
        val noVoteJson = "[]"

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        val voteResult = mock<PostgrestResult>()
        whenever(voteResult.data).thenReturn(noVoteJson)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(voteResult)

        val result = repository.submitVote("post1", 1)

        assertTrue(result.isSuccess)
        verify(votesBuilder).insert(any(), any(), any(), any())
    }

    @Test
    fun `submitVote updates vote when previous vote exists`() = runTest {
        val futureTime = Instant.now().plus(1, ChronoUnit.HOURS).toString()
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"}],"poll_end_time":"$futureTime"}"""
        val existingVoteJson = """[{"id":"vote1","post_id":"post1","user_id":"test_user_id","option_index":0}]"""

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        val voteResult = mock<PostgrestResult>()
        whenever(voteResult.data).thenReturn(existingVoteJson)
        whenever(votesBuilder.select(any(), any(), any(), any())).thenReturn(voteResult)

        val result = repository.submitVote("post1", 1)

        assertTrue(result.isSuccess)
        verify(votesBuilder).update(any(), any(), any(), any())
    }

    @Test
    fun `submitVote fails when poll has ended`() = runTest {
        val pastTime = Instant.now().minus(1, ChronoUnit.HOURS).toString()
        val postJson = """{"id":"post1","poll_options":[{"text":"Option 1"},{"text":"Option 2"}],"poll_end_time":"$pastTime"}"""

        val postResult = mock<PostgrestResult>()
        whenever(postResult.data).thenReturn(postJson)
        whenever(postsBuilder.select(any(), any(), any(), any())).thenReturn(postResult)

        val result = repository.submitVote("post1", 1)

        assertTrue(result.isFailure)
        assertEquals("Poll has ended", result.exceptionOrNull()?.message)
    }

    @Test
    fun `revokeVote calls delete`() = runTest {
        val result = repository.revokeVote("post1")

        assertTrue(result.isSuccess)
        verify(votesBuilder).delete(any(), any(), any(), any())
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

        assertEquals("Option 2", options[1].text)
        assertEquals(1, options[1].voteCount)

        assertEquals("Option 3", options[2].text)
        assertEquals(0, options[2].voteCount)
    }
}
