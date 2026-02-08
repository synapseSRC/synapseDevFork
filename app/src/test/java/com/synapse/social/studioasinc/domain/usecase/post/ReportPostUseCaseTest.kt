package com.synapse.social.studioasinc.domain.usecase.post

import com.synapse.social.studioasinc.data.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ReportPostUseCaseTest {

    @Mock
    private lateinit var repository: ReportRepository

    private lateinit var useCase: ReportPostUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ReportPostUseCase(repository)
    }

    @Test
    fun `invoke should emit success when repository returns success`() = runTest {

        val postId = "post123"
        val reason = "spam"
        val description = "Some description"
        whenever(repository.createReport(postId, reason, description)).thenReturn(Result.success(Unit))


        val result = useCase(postId, reason, description).first()


        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `invoke should emit failure when repository returns failure`() = runTest {

        val postId = "post123"
        val reason = "spam"
        val description = "Some description"
        val exception = Exception("Network error")
        whenever(repository.createReport(postId, reason, description)).thenReturn(Result.failure(exception))


        val result = useCase(postId, reason, description).first()


        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
