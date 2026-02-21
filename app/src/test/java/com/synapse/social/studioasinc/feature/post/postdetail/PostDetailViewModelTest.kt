package com.synapse.social.studioasinc.feature.post.postdetail

import com.synapse.social.studioasinc.data.repository.*
import com.synapse.social.studioasinc.shared.domain.model.*
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

        whenever(client.pluginManager).thenReturn(pluginManager)




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

        whenever(postDetailRepository.getPostWithDetails(postId)).thenReturn(Result.success(mockPostDetail))
        viewModel.loadPost(postId)
        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()


        val reactionType = ReactionType.LIKE
        whenever(reactionRepository.toggleReaction(postId, "post", reactionType))
            .thenReturn(Result.success(ReactionToggleResult.ADDED))


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


        val mockPostDetail = PostDetail(
            post = Post(id = postId),
            author = UserProfile(uid = "author_1", username = "testuser")
        )
        whenever(postDetailRepository.getPostWithDetails(postId)).thenReturn(Result.success(mockPostDetail))
        viewModel.loadPost(postId)
        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()


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
