package com.synapse.social.studioasinc.ui.inbox

import com.synapse.social.studioasinc.ui.inbox.models.InboxUiState
import com.synapse.social.studioasinc.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun loadData_setsStateToSuccess() = runTest {
        val viewModel = InboxViewModel()

        // Advance time on the scheduler
        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was $state", state is InboxUiState.Success)
    }
}
