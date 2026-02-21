package com.synapse.social.studioasinc.ui.home

import android.app.Application
import androidx.paging.PagingData
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import com.synapse.social.studioasinc.data.repository.PostRepository
import com.synapse.social.studioasinc.data.repository.SettingsRepository
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.domain.usecase.post.BookmarkPostUseCase
import com.synapse.social.studioasinc.domain.usecase.post.ReactToPostUseCase
import com.synapse.social.studioasinc.domain.usecase.post.RevokeVoteUseCase
import com.synapse.social.studioasinc.domain.usecase.post.VotePollUseCase
import com.synapse.social.studioasinc.feature.home.home.FeedViewModel
import com.synapse.social.studioasinc.ui.settings.AppearanceSettings
import com.synapse.social.studioasinc.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FeedViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val postRepository: PostRepository = mock()
    private val authRepository: AuthRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val reactToPostUseCase: ReactToPostUseCase = mock()
    private val votePollUseCase: VotePollUseCase = mock()
    private val revokeVoteUseCase: RevokeVoteUseCase = mock()
    private val bookmarkPostUseCase: BookmarkPostUseCase = mock()
    private val application: Application = mock()

    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        whenever(postRepository.getPostsPaged()).thenReturn(flowOf(PagingData.empty()))
        whenever(settingsRepository.appearanceSettings).thenReturn(flowOf(AppearanceSettings()))

        viewModel = FeedViewModel(
            postRepository,
            authRepository,
            settingsRepository,
            reactToPostUseCase,
            votePollUseCase,
            revokeVoteUseCase,
            bookmarkPostUseCase,
            application
        )
    }

    @Test
    fun `testModifiedPostsLimit enforces max size`() = runTest {
        val maxPosts = 100
        val totalPosts = 150

        whenever(reactToPostUseCase(any(), any())).thenAnswer { invocation ->
            val post = invocation.arguments[0] as Post
            flowOf(Result.success(post.copy(likesCount = post.likesCount + 1)))
        }

        for (i in 1..totalPosts) {
            val post = Post(id = "post_$i", authorUid = "author_$i", likesCount = 0)
            viewModel.likePost(post)

            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
        }

        assertEquals(maxPosts, viewModel.getModifiedPostsCount())
    }
}
