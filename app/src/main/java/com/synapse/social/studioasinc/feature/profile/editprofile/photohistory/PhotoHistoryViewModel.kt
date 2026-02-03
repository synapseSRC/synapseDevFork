package com.synapse.social.studioasinc.feature.profile.editprofile.photohistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.feature.profile.editprofile.EditProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoHistoryUiState(
    val isLoading: Boolean = true,
    val items: List<HistoryItem> = emptyList(),
    val error: String? = null,
    val currentPhotoUrl: String? = null,
    val photoType: PhotoType = PhotoType.PROFILE
)

sealed class PhotoHistoryEvent {
    data class LoadHistory(val type: PhotoType) : PhotoHistoryEvent()
    data class SetAsCurrent(val item: HistoryItem) : PhotoHistoryEvent()
    data class DeleteItem(val item: HistoryItem) : PhotoHistoryEvent()
}

@HiltViewModel
class PhotoHistoryViewModel @Inject constructor(
    private val repository: EditProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoHistoryUiState())
    val uiState: StateFlow<PhotoHistoryUiState> = _uiState.asStateFlow()

    fun onEvent(event: PhotoHistoryEvent) {
        when (event) {
            is PhotoHistoryEvent.LoadHistory -> loadHistory(event.type)
            is PhotoHistoryEvent.SetAsCurrent -> setAsCurrent(event.item)
            is PhotoHistoryEvent.DeleteItem -> deleteItem(event.item)
        }
    }

    private fun loadHistory(type: PhotoType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, photoType = type) }
            val userId = repository.getCurrentUserId()
            if (userId == null) {
                 _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                 return@launch
            }

            // Load current profile to know which one is selected
            val profileResult = repository.getUserProfile(userId)

            // First fetch history
            val historyFlow = when (type) {
                PhotoType.PROFILE -> repository.getProfileHistory(userId)
                PhotoType.COVER -> repository.getCoverHistory(userId)
            }

            historyFlow.collect { result ->
                result.fold(
                    onSuccess = { items ->
                        _uiState.update { it.copy(items = items) }
                        // After loading history, check current profile to mark selected
                        fetchCurrentProfile(userId, type)
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                )
            }
        }
    }

    private suspend fun fetchCurrentProfile(userId: String, type: PhotoType) {
         repository.getUserProfile(userId).collect { result ->
             result.fold(
                 onSuccess = { profile ->
                     val currentUrl = when (type) {
                         PhotoType.PROFILE -> profile.avatar
                         PhotoType.COVER -> profile.profileCoverImage
                     }
                     _uiState.update { it.copy(isLoading = false, currentPhotoUrl = currentUrl) }
                 },
                 onFailure = {
                     _uiState.update { it.copy(isLoading = false) } // Ignore profile load error, just show history
                 }
             )
         }
    }

    private fun setAsCurrent(item: HistoryItem) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: return@launch
            val type = _uiState.value.photoType

            val isCurrent = item.imageUrl == _uiState.value.currentPhotoUrl
            val newUrl = if (isCurrent) "null" else item.imageUrl

            val updateData = mutableMapOf<String, String>()
            when (type) {
                PhotoType.PROFILE -> updateData["avatar"] = newUrl
                PhotoType.COVER -> updateData["profile_cover_image"] = newUrl
            }

            val result = repository.updateProfile(userId, updateData)

            result.fold(
                onSuccess = {
                     val displayedUrl = if (newUrl == "null") null else newUrl
                     _uiState.update { it.copy(currentPhotoUrl = displayedUrl) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = "Failed to update profile: ${error.message}") }
                }
            )
        }
    }

    private fun deleteItem(item: HistoryItem) {
        viewModelScope.launch {
            val type = _uiState.value.photoType
            val userId = repository.getCurrentUserId() ?: return@launch

            // If deleting current, reset current first
            if (item.imageUrl == _uiState.value.currentPhotoUrl) {
                 val updateData = mutableMapOf<String, String>()
                 when (type) {
                    PhotoType.PROFILE -> updateData["avatar"] = "null"
                    PhotoType.COVER -> updateData["profile_cover_image"] = "null"
                 }
                 repository.updateProfile(userId, updateData)
                 _uiState.update { it.copy(currentPhotoUrl = null) }
            }

            val result = when (type) {
                PhotoType.PROFILE -> repository.deleteProfileHistoryItem(item.key)
                PhotoType.COVER -> repository.deleteCoverHistoryItem(item.key)
            }

            result.fold(
                onSuccess = {
                    // Reload history
                    loadHistory(type)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = "Failed to delete item: ${error.message}") }
                }
            )
        }
    }
}
