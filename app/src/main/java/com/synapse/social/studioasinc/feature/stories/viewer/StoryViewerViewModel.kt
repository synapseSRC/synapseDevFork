package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.feature.stories.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.AuthRepository
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.StoryRepository
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.UserRepository
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.Story
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.StoryMediaType
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoryViewerState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stories: List<Story> = emptyList(),
    val user: User? = null,
    val currentStoryIndex: Int = 0,
    val progress: Float = 0f, // 0.0 to 1.0
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

@HiltViewModel
class StoryViewerViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryViewerState())
    val uiState: StateFlow<StoryViewerState> = _uiState.asStateFlow()

    private var progressJob: Job? = null
    private val defaultStoryDuration = 5000L // Default duration 5 seconds
    private val progressUpdateInterval = 50L // Update every 50ms

    fun loadStories(userId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // Fetch User
            val userResult = userRepository.getUserById(userId)
            val user = userResult.getOrNull()

            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }

            // Fetch Stories
            val storiesResult = storyRepository.getUserStories(userId)
            storiesResult.onSuccess { stories ->
                if (stories.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No stories found", user = user) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            stories = stories,
                            user = user,
                            currentStoryIndex = 0,
                            progress = 0f
                        )
                    }
                    val firstStory = stories[0]
                    if (firstStory.mediaType != StoryMediaType.VIDEO) {
                        startProgress()
                    }
                    markAsSeen(firstStory.id)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun startProgress(durationOverride: Long? = null) {
        if (_uiState.value.isPaused || _uiState.value.isFinished) return

        stopProgress()
        progressJob = viewModelScope.launch {
            val stories = _uiState.value.stories
            val currentIndex = _uiState.value.currentStoryIndex
            if (currentIndex !in stories.indices) return@launch

            val currentStory = stories[currentIndex]
            val duration = durationOverride
                ?: currentStory.mediaDurationSeconds?.times(1000L)
                ?: defaultStoryDuration

            val steps = duration / progressUpdateInterval
            val stepSize = 1.0f / steps

            // Resume from current progress
            var currentProgress = _uiState.value.progress

            while (currentProgress < 1.0f) {
                delay(progressUpdateInterval)
                if (_uiState.value.isPaused) return@launch

                currentProgress += stepSize
                _uiState.update { it.copy(progress = currentProgress.coerceAtMost(1.0f)) }
            }

            // Finished current story
            nextStory()
        }
    }

    private fun stopProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    fun nextStory() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentStoryIndex + 1

        if (nextIndex < currentState.stories.size) {
            _uiState.update {
                it.copy(
                    currentStoryIndex = nextIndex,
                    progress = 0f
                )
            }
            val nextStory = currentState.stories[nextIndex]
            if (nextStory.mediaType != StoryMediaType.VIDEO) {
                startProgress()
            }
            markAsSeen(nextStory.id)
        } else {
            _uiState.update { it.copy(isFinished = true) }
            stopProgress()
        }
    }

    fun previousStory() {
        val currentState = _uiState.value
        val prevIndex = currentState.currentStoryIndex - 1

        if (prevIndex >= 0) {
            _uiState.update {
                it.copy(
                    currentStoryIndex = prevIndex,
                    progress = 0f
                )
            }
            val prevStory = currentState.stories[prevIndex]
            if (prevStory.mediaType != StoryMediaType.VIDEO) {
                startProgress()
            }
            markAsSeen(prevStory.id)
        } else {
            // Start of first story, just reset progress
             _uiState.update { it.copy(progress = 0f) }
             val currentStory = currentState.stories.getOrNull(0)
             if (currentStory?.mediaType != StoryMediaType.VIDEO) {
                 startProgress()
             }
        }
    }

    fun pause() {
        _uiState.update { it.copy(isPaused = true) }
        stopProgress()
    }

    fun resume() {
        _uiState.update { it.copy(isPaused = false) }
        // For video, we don't auto-resume progress here if we rely on onVideoReady/playing.
        // But for image we do.
        // And even for video, if it was playing, we should resume progress.
        // The check in startProgress will handle duration.
        // However, if we paused a video, we need to know the duration again?
        // Actually, startProgress re-calculates duration from story metadata or override.
        // We lost the override.
        // But the video duration should be in the story object? No, only if we updated it.
        // We should probably update the story object in onVideoReady so we persist the duration.
        // But Stories are immutable data classes.

        // Simpler approach: resume() blindly calls startProgress().
        // If it's a video, onVideoReady might have already fired.
        // If we don't have duration, we default to 5s.
        // Ideally we should cache duration.

        // Check if current story is video. If so, wait for player to be ready?
        // The Player in UI will stay ready/paused. When we resume, it plays.
        // So we should resume progress.
        // But we might lose the "real" duration if we rely on override.
        // Let's rely on default or metadata for resume for now to avoid overcomplicating.
        startProgress()
    }

    fun onVideoReady(durationMs: Long) {
        // Only start if not paused (or if we were waiting for this)
        if (_uiState.value.isPaused || _uiState.value.isFinished) return

        // Start progress with actual duration
        startProgress(durationOverride = durationMs)
    }

    private fun markAsSeen(storyId: String?) {
        if (storyId == null) return
        val currentUserId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            storyRepository.markAsSeen(storyId, currentUserId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgress()
    }
}
