package com.synapse.social.studioasinc.ui.home

import android.app.Application
import androidx.paging.PagingData
import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.data.repository.PostRepository
import com.synapse.social.studioasinc.data.repository.PollRepository
import com.synapse.social.studioasinc.data.repository.ReactionRepository
import com.synapse.social.studioasinc.data.repository.SettingsRepository
import com.synapse.social.studioasinc.domain.model.Post
import com.synapse.social.studioasinc.ui.settings.AppearanceSettings
import com.synapse.social.studioasinc.ui.settings.PostViewStyle
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
    private val reactionRepository: ReactionRepository = mock()
    private val pollRepository: PollRepository = mock()
    private val application: Application = mock()

    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        // Mock required flows
        whenever(postRepository.getPostsPaged()).thenReturn(flowOf(PagingData.empty()))
        whenever(settingsRepository.appearanceSettings).thenReturn(flowOf(AppearanceSettings()))

        viewModel = FeedViewModel(
            postRepository,
            authRepository,
            settingsRepository,
            reactionRepository,
            pollRepository,
            application
        )
    }

    @Test
    fun `testModifiedPostsLimit enforces max size`() = runTest {
        // Given
        val maxPosts = 100 // Should match MAX_MODIFIED_POSTS in ViewModel
        val totalPosts = 150

        // When: Like 150 different posts
        for (i in 1..totalPosts) {
            val post = Post(id = "post_$i", authorUid = "author_$i", likesCount = 0)
            viewModel.likePost(post)
            // Advance dispatcher to allow coroutine to run
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
        }

        // Then: Size should be capped at 100
        assertEquals(maxPosts, viewModel.getModifiedPostsCount())
    }
}
