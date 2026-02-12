package com.synapse.social.studioasinc.feature.profile.editprofile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.shared.domain.model.Gender
import com.synapse.social.studioasinc.shared.domain.model.UserProfile
import com.synapse.social.studioasinc.shared.domain.usecase.*
import com.synapse.social.studioasinc.core.util.FileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    application: Application,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileSectionUseCase: UpdateProfileSectionUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val checkUsernameAvailabilityUseCase: CheckUsernameAvailabilityUseCase,
    private val syncUsernameChangeUseCase: SyncUsernameChangeUseCase,
    private val getCurrentUserIdUseCase: com.synapse.social.studioasinc.shared.domain.usecase.GetCurrentUserIdUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<EditProfileNavigation>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    private var lastAvatarUri: Uri? = null
    private var lastCoverUri: Uri? = null

    init {
        loadUserProfile()
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.UsernameChanged -> {
                _uiState.update { it.copy(username = event.username, hasChanges = true) }
                validateUsername(event.username)
            }
            is EditProfileEvent.NicknameChanged -> {
                _uiState.update { it.copy(nickname = event.nickname, hasChanges = true) }
                validateNickname(event.nickname)
            }
            is EditProfileEvent.BiographyChanged -> {
                _uiState.update { it.copy(bio = event.bio, hasChanges = true) }
                validateBio(event.bio)
            }
            is EditProfileEvent.GenderSelected -> {
                _uiState.update { it.copy(selectedGender = event.gender, hasChanges = true) }
            }
            is EditProfileEvent.RegionSelected -> {
                _uiState.update { it.copy(selectedRegion = event.region, hasChanges = true) }
            }
            is EditProfileEvent.AvatarSelected -> handleAvatarSelection(event.uri)
            is EditProfileEvent.CoverSelected -> handleCoverSelection(event.uri)
            EditProfileEvent.RetryAvatarUpload -> retryAvatarUpload()
            EditProfileEvent.RetryCoverUpload -> retryCoverUpload()
            EditProfileEvent.SaveClicked -> saveProfile()
            EditProfileEvent.BackClicked -> viewModelScope.launch { _navigationEvents.emit(EditProfileNavigation.NavigateBack) }
            EditProfileEvent.ProfileHistoryClicked -> viewModelScope.launch { _navigationEvents.emit(EditProfileNavigation.NavigateToProfileHistory) }
            EditProfileEvent.CoverHistoryClicked -> viewModelScope.launch { _navigationEvents.emit(EditProfileNavigation.NavigateToCoverHistory) }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = getCurrentUserIdUseCase()
            if (userId != null) {
                getProfileUseCase(userId).collect { result ->
                    result.onSuccess { profile ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                profile = profile,
                                username = profile.username ?: "",
                                nickname = profile.displayName ?: "",
                                bio = profile.bio ?: "",
                                selectedGender = profile.gender ?: Gender.Hidden,
                                selectedRegion = profile.region,
                                avatarUrl = profile.avatar,
                                coverUrl = profile.profileCoverImage
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
            }
        }
    }

    private fun validateUsername(username: String) {
        if (username.length < 3) {
            _uiState.update { it.copy(usernameValidation = UsernameValidation.Error("Username too short")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(usernameValidation = UsernameValidation.Checking) }
            val userId = getCurrentUserIdUseCase() ?: return@launch
            val result = checkUsernameAvailabilityUseCase(username, userId)
            result.onSuccess { available ->
                if (available) {
                    _uiState.update { it.copy(usernameValidation = UsernameValidation.Valid) }
                } else {
                    _uiState.update { it.copy(usernameValidation = UsernameValidation.Error("Username taken")) }
                }
            }
        }
    }

    private fun validateNickname(nickname: String) { /* No-op for now */ }
    private fun validateBio(bio: String) { /* No-op for now */ }

    private fun handleAvatarSelection(uri: Uri) {
        lastAvatarUri = uri
        _uiState.update { it.copy(avatarUploadState = UploadState.Uploading()) }
        viewModelScope.launch {
            processAndUploadImage(uri, true)
        }
    }

    private fun handleCoverSelection(uri: Uri) {
        lastCoverUri = uri
        _uiState.update { it.copy(coverUploadState = UploadState.Uploading()) }
        viewModelScope.launch {
            processAndUploadImage(uri, false)
        }
    }

    private suspend fun processAndUploadImage(uri: Uri, isAvatar: Boolean) {
        try {
            val context = getApplication<Application>()
            var realFilePath = FileManager.getPathFromUri(context, uri)

            if (realFilePath == null) {
                val tempInputFile = File(context.cacheDir, "temp_input_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempInputFile.outputStream().use { output -> input.copyTo(output) }
                }
                realFilePath = tempInputFile.absolutePath
            }

            val tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileManager.resizeBitmapFileRetainRatio(realFilePath, tempFile.absolutePath, 1024)

            val userId = getCurrentUserIdUseCase() ?: throw Exception("User not logged in")

            val result = if (isAvatar) {
                uploadProfileImageUseCase.uploadAvatar(userId, tempFile.absolutePath)
            } else {
                uploadProfileImageUseCase.uploadCover(userId, tempFile.absolutePath)
            }

            result.onSuccess { url ->
                _uiState.update {
                    if (isAvatar) it.copy(avatarUrl = url, avatarUploadState = UploadState.Success, hasChanges = true)
                    else it.copy(coverUrl = url, coverUploadState = UploadState.Success, hasChanges = true)
                }
            }.onFailure { error ->
                _uiState.update {
                    if (isAvatar) it.copy(avatarUploadState = UploadState.Error(error.message ?: "Upload failed"))
                    else it.copy(coverUploadState = UploadState.Error(error.message ?: "Upload failed"))
                }
            }
        } catch (e: Exception) {
             _uiState.update {
                if (isAvatar) it.copy(avatarUploadState = UploadState.Error(e.message ?: "Processing failed"))
                else it.copy(coverUploadState = UploadState.Error(e.message ?: "Processing failed"))
            }
        }
    }

    private fun retryAvatarUpload() { lastAvatarUri?.let { handleAvatarSelection(it) } }
    private fun retryCoverUpload() { lastCoverUri?.let { handleCoverSelection(it) } }

    private fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val userId = getCurrentUserIdUseCase() ?: return@launch

            val result = updateProfileSectionUseCase.updateBasicInfo(
                userId, state.username, state.nickname, state.bio,
                state.selectedGender.name.lowercase(), state.selectedRegion
            )

            result.onSuccess {
                val originalUsername = state.profile?.username
                if (originalUsername != null && originalUsername != state.username) {
                     syncUsernameChangeUseCase(originalUsername, state.username, userId)
                }

                _uiState.update { it.copy(isSaving = false, hasChanges = false) }
                _navigationEvents.emit(EditProfileNavigation.NavigateBack)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
