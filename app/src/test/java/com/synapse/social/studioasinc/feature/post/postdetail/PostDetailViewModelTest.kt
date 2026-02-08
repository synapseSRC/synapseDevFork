package com.synapse.social.studioasinc.feature.post.postdetail

import com.synapse.social.studioasinc.data.repository.*
import com.synapse.social.studioasinc.domain.model.*
import com.synapse.social.studioasinc.util.MainCoroutineRule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.plugins.PluginManager
import io.github.jan.supabase.plugins.SupabasePluginProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*

/**
 * Unit tests for [PostDetailViewModel].
 *
 * Note: These tests require 'mock-maker-inline' to be enabled for Mockito because
 * Supabase's [PluginManager] is a final class and [SupabaseClientConfig] is internal,
 * making it impossible to implement a fake or mock without inline mocking.
 *
 * A 'mock-maker-inline' file has been added to test resources, but if tests fail with
 * InvalidUseOfMatchersException or IllegalStateException (Plugin not found), checks build config.
 */
@ExperimentalCoroutinesApi
@Ignore("Requires mock-maker-inline enabled in Gradle/Mockito environment to mock final PluginManager")
class PostDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val postDetailRepository: PostDetailRepository = mock()
    private val commentRepository: CommentRepository = mock()
    private val reactionRepository: ReactionRepository = mock()
    private val pollRepository: PollRepository = mock()
    private val bookmarkRepository: BookmarkRepository = mock()
    private val reshareRepository: ReshareRepository = mock()
    private val reportRepository: ReportRepository = mock()
    private val userRepository: UserRepository = mock()
    private val client: SupabaseClient = mock()
    private val auth: Auth = mock()
    private val pluginManager: PluginManager = mock()

    private val session: UserSession = mock()
    private val user: UserInfo = mock()

    private lateinit var viewModel: PostDetailViewModel

    @Before
    fun setUp() {
        // Mock Supabase Client structure
        whenever(client.pluginManager).thenReturn(pluginManager)

        // Mock getPlugin(Auth)
        // Note: This matches the Auth companion object key used in SupabaseClient.auth extension
        // Requires inline mocking for final PluginManager
        whenever(pluginManager.getPlugin(eq(Auth))).thenReturn(auth)

        whenever(auth.currentSessionOrNull()).thenReturn(session)
        whenever(session.user).thenReturn(user)
        whenever(user.id).thenReturn("test_user_id")

        viewModel = PostDetailViewModel(
            postDetailRepository,
            commentRepository,
            reactionRepository,
            pollRepository,
            bookmarkRepository,
            reshareRepository,
            reportRepository,
            userRepository,
            client
        )
    }

    @Test
    fun `loadPost success updates uiState`() = runTest {
        val postId = "post_123"
        val mockPostDetail = PostDetail(
            post = Post(id = postId, authorUid = "author_1"),
            author = UserProfile(uid = "author_1", username = "testuser"),
            reactionSummary = emptyMap(),
            userReaction = null,
            isBookmarked = false,
            hasReshared = false,
            pollResults = null,
            userPollVote = null
        )

        whenever(postDetailRepository.getPostWithDetails(postId)).thenReturn(Result.success(mockPostDetail))
        whenever(postDetailRepository.incrementViewCount(postId)).thenReturn(Result.success(Unit))

        viewModel.loadPost(postId)

        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(mockPostDetail, state.post)
        assertEquals(false, state.isLoading)
        assertNull(state.error)

        verify(postDetailRepository).getPostWithDetails(postId)
        verify(postDetailRepository).incrementViewCount(postId)
    }

    @Test
    fun `loadPost failure updates uiState with error`() = runTest {
        val postId = "post_123"
        val errorMessage = "Network Error"

        whenever(postDetailRepository.getPostWithDetails(postId)).thenReturn(Result.failure(Exception(errorMessage)))
        whenever(postDetailRepository.incrementViewCount(postId)).thenReturn(Result.success(Unit))

        viewModel.loadPost(postId)

        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.post)
        assertEquals(false, state.isLoading)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `toggleReaction success refreshes post`() = runTest {
        val postId = "post_123"
        val mockPostDetail = PostDetail(
            post = Post(id = postId, authorUid = "author_1"),
            author = UserProfile(uid = "author_1", username = "testuser"),
            reactionSummary = emptyMap(),
            userReaction = null
        )
        // Setup initial state
        whenever(postDetailRepository.getPostWithDetails(postId)).thenReturn(Result.success(mockPostDetail))
        viewModel.loadPost(postId)
        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        // Setup reaction toggle
        val reactionType = ReactionType.LIKE
        whenever(reactionRepository.toggleReaction(postId, "post", reactionType))
            .thenReturn(Result.success(ReactionToggleResult.ADDED))

        // Setup refresh call (returns updated post)
        val updatedPostDetail = mockPostDetail.copy(userReaction = reactionType)
        whenever(postDetailRepository.getPostWithDetails(postId)).thenReturn(Result.success(updatedPostDetail))

        viewModel.toggleReaction(reactionType)
        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(updatedPostDetail, state.post)
        verify(reactionRepository).toggleReaction(postId, "post", reactionType)
        verify(postDetailRepository, times(2)).getPostWithDetails(postId)
    }

    @Test
    fun `addComment success refreshes comments`() = runTest {
        val postId = "post_123"
        val commentContent = "Nice post!"

        // Setup initial state
        val mockPostDetail = PostDetail(
            post = Post(id = postId),
            author = UserProfile(uid = "author_1", username = "testuser")
        )
        whenever(postDetailRepository.getPostWithDetails(postId)).thenReturn(Result.success(mockPostDetail))
        viewModel.loadPost(postId)
        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        // Mock createComment
        val mockComment = CommentWithUser(
            id = "comment_1",
            postId = postId,
            userId = "user_1",
            content = commentContent,
            createdAt = "2023-01-01T12:00:00Z"
        )
        whenever(commentRepository.createComment(eq(postId), eq(commentContent), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(mockComment))

        viewModel.addComment(commentContent)
        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        verify(commentRepository).createComment(eq(postId), eq(commentContent), anyOrNull(), anyOrNull())
        verify(postDetailRepository, times(2)).getPostWithDetails(postId)
    }
}
