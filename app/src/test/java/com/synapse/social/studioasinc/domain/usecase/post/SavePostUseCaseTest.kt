package com.synapse.social.studioasinc.shared.domain.usecase.post

import com.synapse.social.studioasinc.shared.data.repository.PostInteractionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SavePostUseCaseTest {

    @Mock
    private lateinit var repository: PostInteractionRepository

    private lateinit var useCase: SavePostUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = SavePostUseCase(repository)
    }

    @Test
    fun `invoke should emit success when repository returns success`() = runTest {
        // Given
        val postId = "post123"
        val userId = "user456"
        whenever(repository.savePost(postId, userId)).thenReturn(Result.success(Unit))

        // When
        val result = useCase(postId, userId).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(repository).savePost(postId, userId)
    }

    @Test
    fun `invoke should emit failure when repository returns failure`() = runTest {
        // Given
        val postId = "post123"
        val userId = "user456"
        val exception = Exception("Failed to save post")
        whenever(repository.savePost(postId, userId)).thenReturn(Result.failure(exception))

        // When
        val result = useCase(postId, userId).first()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(repository).savePost(postId, userId)
    }
}
