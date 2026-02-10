package com.synapse.social.studioasinc.ui.createpost

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.data.remote.services.SupabaseAuthenticationService
import com.synapse.social.studioasinc.data.repository.PostRepository
import com.synapse.social.studioasinc.data.repository.LocationRepository
import com.synapse.social.studioasinc.domain.model.MediaItem
import com.synapse.social.studioasinc.domain.model.MediaType
import com.synapse.social.studioasinc.domain.model.PollOption
import com.synapse.social.studioasinc.domain.model.Post
import com.synapse.social.studioasinc.domain.model.User
import com.synapse.social.studioasinc.domain.model.UserProfile
import com.synapse.social.studioasinc.domain.model.LocationData
import com.synapse.social.studioasinc.core.util.FileManager
import com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.core.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.synapse.social.studioasinc.domain.model.PostMetadata
import com.synapse.social.studioasinc.domain.model.FeelingActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap
import com.synapse.social.studioasinc.domain.model.FeelingType
import com.synapse.social.studioasinc.shared.data.repository.ReelRepository
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

enum class CompositionType {
    POST, REEL
}

data class CreatePostUiState(
    val compositionType: CompositionType = CompositionType.POST,
    val isLoading: Boolean = false,
    val postText: String = "",
    val mediaItems: List<MediaItem> = emptyList(),
    val pollData: PollData? = null,
    val location: LocationData? = null,
    val youtubeUrl: String? = null,
    val privacy: String = "public",
    val settings: PostSettings = PostSettings(),
    val error: String? = null,
    val isPostCreated: Boolean = false,
    val uploadProgress: Float = 0f,
    val isEditMode: Boolean = false,
    val checkDraft: Boolean = true,
    val currentUserProfile: User? = null,

    val taggedPeople: List<User> = emptyList(),
    val feeling: FeelingActivity? = null,
    val textBackgroundColor: Long? = null,

    val userSearchResults: List<User> = emptyList(),
    val locationSearchResults: List<LocationData> = emptyList(),
    val feelingSearchResults: List<FeelingActivity> = emptyList(),
    val isSearchLoading: Boolean = false
)

data class PollData(
    val question: String,
    val options: List<String>,
    val durationHours: Int
)

data class PostSettings(
    val hideViewsCount: Boolean = false,
    val hideLikeCount: Boolean = false,
    val hideCommentsCount: Boolean = false,
    val disableComments: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    application: Application,
    private val postRepository: PostRepository,
    private val reelRepository: ReelRepository,
    private val userRepository: com.synapse.social.studioasinc.data.repository.UserRepository,
    private val locationRepository: LocationRepository,
    private val imageCompressor: ImageCompressor,
    private val uploadMediaUseCase: UploadMediaUseCase
) : AndroidViewModel(application) {

    private val authService = SupabaseAuthenticationService()
    private val prefs = application.getSharedPreferences("create_post_draft", Context.MODE_PRIVATE)


    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()


    private var editPostId: String? = null
    private var originalPost: Post? = null

    private var userSearchJob: Job? = null
    private var locationSearchJob: Job? = null

    private val allFeelings = listOf(
        FeelingActivity("😊", "Happy", FeelingType.MOOD),
        FeelingActivity("😎", "Cool", FeelingType.MOOD),
        FeelingActivity("😍", "Loved", FeelingType.MOOD),
        FeelingActivity("😢", "Sad", FeelingType.MOOD),
        FeelingActivity("🥳", "Celebrating", FeelingType.ACTIVITY),
        FeelingActivity("😴", "Tired", FeelingType.MOOD),
        FeelingActivity("😡", "Angry", FeelingType.MOOD),
        FeelingActivity("🤔", "Thinking", FeelingType.MOOD),
        FeelingActivity("🤒", "Sick", FeelingType.MOOD),
        FeelingActivity("✈️", "Traveling", FeelingType.ACTIVITY),
        FeelingActivity("🍴", "Eating", FeelingType.ACTIVITY),
        FeelingActivity("🎮", "Playing", FeelingType.ACTIVITY),
        FeelingActivity("📚", "Reading", FeelingType.ACTIVITY),
        FeelingActivity("📺", "Watching", FeelingType.ACTIVITY),
        FeelingActivity("🎧", "Listening to music", FeelingType.ACTIVITY)
    )

    init {


        loadCurrentUser()

        _uiState.update { it.copy(feelingSearchResults = allFeelings) }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authService.getCurrentUserId()?.let { uid ->

                userRepository.getUserById(uid).onSuccess { user ->
                    _uiState.update { it.copy(currentUserProfile = user) }


                    try {
                        val freshUserProfile = SupabaseClient.client.from("users")
                            .select {
                                filter { eq("uid", uid) }
                            }.decodeSingleOrNull<UserProfile>()

                        freshUserProfile?.let { profile ->
                             val updatedUser = user?.copy(
                                 avatar = profile.avatar,
                                 displayName = profile.displayName,
                                 username = profile.username
                             ) ?: User(
                                 uid = profile.uid,
                                 username = profile.username,
                                 displayName = profile.displayName,
                                 avatar = profile.avatar,
                                 email = profile.email,
                                 verify = profile.verify
                             )
                             _uiState.update { it.copy(currentUserProfile = updatedUser) }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CreatePostViewModel", "Failed to fetch fresh user profile", e)
                    }
                }
            }
        }
    }

    fun loadDraft() {
        if (_uiState.value.isEditMode || !_uiState.value.checkDraft) return

        val draftText = prefs.getString("draft_text", null)


        if (!draftText.isNullOrEmpty()) {
             _uiState.update { it.copy(postText = draftText, checkDraft = false) }
        } else {
            _uiState.update { it.copy(checkDraft = false) }
        }
    }

    fun saveDraft() {
        if (_uiState.value.isPostCreated) return
        if (_uiState.value.isEditMode) return

        val text = _uiState.value.postText


        if (text.isNotBlank() || _uiState.value.mediaItems.isNotEmpty()) {
            prefs.edit()
                .putString("draft_text", text)
                .apply()
        }
    }

    fun clearDraft() {
        prefs.edit()
            .remove("draft_text")
            .apply()
    }

    fun setCompositionType(type: String) {
        val compositionType = try {
            CompositionType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            CompositionType.POST
        }
        _uiState.update { it.copy(compositionType = compositionType) }
    }

    fun loadPostForEdit(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditMode = true, checkDraft = false) }
            postRepository.getPost(postId).onSuccess { post ->
                post?.let {
                    originalPost = it
                    editPostId = it.id

                    val mediaItems = it.mediaItems?.toMutableList() ?: mutableListOf()

                    if (mediaItems.isEmpty()) {
                        it.postImage?.let { imgUrl ->
                             mediaItems.add(MediaItem(url = imgUrl, type = MediaType.IMAGE))
                        }
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            postText = it.postText ?: "",
                            mediaItems = mediaItems,
                            privacy = it.postVisibility ?: "public",
                            youtubeUrl = it.youtubeUrl,
                            settings = PostSettings(
                                hideViewsCount = it.postHideViewsCount == "true",
                                hideLikeCount = it.postHideLikeCount == "true",
                                hideCommentsCount = it.postHideCommentsCount == "true",
                                disableComments = it.postDisableComments == "true"
                            ),

                            pollData = if (it.hasPoll == true) PollData(it.pollQuestion ?: "", it.pollOptions?.map { opt -> opt.text } ?: emptyList(), 24) else null,
                            location = if (it.hasLocation == true) LocationData(it.locationName ?: "", it.locationAddress, it.locationLatitude, it.locationLongitude) else null,
                            taggedPeople = it.metadata?.taggedPeople ?: emptyList(),
                            feeling = it.metadata?.feeling,
                            textBackgroundColor = it.metadata?.backgroundColor
                        )
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load post for editing") }
            }
        }
    }

    fun updateText(text: String) {
        _uiState.update { it.copy(postText = text) }
    }

    fun addTaggedPerson(user: User) {
        val current = _uiState.value.taggedPeople.toMutableList()

        if (current.none { it.uid == user.uid }) {
            current.add(user)
            _uiState.update { it.copy(taggedPeople = current) }
        }
    }

    fun removeTaggedPerson(user: User) {
        val current = _uiState.value.taggedPeople.toMutableList()
        current.removeAll { it.uid == user.uid }
        _uiState.update { it.copy(taggedPeople = current) }
    }


    fun toggleTaggedPerson(user: User) {
        val current = _uiState.value.taggedPeople
        if (current.any { it.uid == user.uid }) {
            removeTaggedPerson(user)
        } else {
            addTaggedPerson(user)
        }
    }

    fun setFeelingActivity(feeling: FeelingActivity?) {
        _uiState.update { it.copy(feeling = feeling) }
    }

    fun setTextBackgroundColor(color: Long?) {
        _uiState.update { it.copy(textBackgroundColor = color) }
    }

    fun addMedia(uris: List<Uri>) {
        val currentMedia = _uiState.value.mediaItems.toMutableList()
        val context = getApplication<Application>()

        uris.forEach { uri ->

             android.util.Log.d("CreatePost", "Processing URI: $uri")
             val mimeType = context.contentResolver.getType(uri) ?: return@forEach
             val type = if (mimeType.startsWith("video")) MediaType.VIDEO else MediaType.IMAGE
             FileManager.getPathFromUri(context, uri)?.let { path ->
                 android.util.Log.d("CreatePost", "Converted URI to path: $path")
                 currentMedia.add(MediaItem(url = path, type = type))
             } ?: run {
                 android.util.Log.e("CreatePost", "Failed to convert URI to path: $uri")
             }
        }
        _uiState.update { it.copy(mediaItems = currentMedia, error = null) }
    }

    fun removeMedia(index: Int) {
        val currentMedia = _uiState.value.mediaItems.toMutableList()
        if (index in currentMedia.indices) {
            currentMedia.removeAt(index)
            _uiState.update { it.copy(mediaItems = currentMedia) }
        }
    }

    fun updateMediaItem(index: Int, uri: Uri) {
        val currentMedia = _uiState.value.mediaItems.toMutableList()
        val context = getApplication<Application>()

        if (index in currentMedia.indices) {
            FileManager.getPathFromUri(context, uri)?.let { path ->
                val oldItem = currentMedia[index]


                val newItem = oldItem.copy(
                    url = path,
                    mimeType = context.contentResolver.getType(uri)
                )
                currentMedia[index] = newItem
                _uiState.update { it.copy(mediaItems = currentMedia) }
            } ?: run {
                android.util.Log.e("CreatePost", "Failed to convert edited URI to path: $uri")
                _uiState.update { it.copy(error = "Failed to save edited image") }
            }
        }
    }

    fun setPoll(pollData: PollData?) {
        _uiState.update { it.copy(pollData = pollData, mediaItems = emptyList()) }
    }

    fun setLocation(location: LocationData?) {
        _uiState.update { it.copy(location = location) }
    }

    fun setYoutubeUrl(url: String?) {
        _uiState.update { it.copy(youtubeUrl = url) }
    }

    fun setPrivacy(privacy: String) {
        _uiState.update { it.copy(privacy = privacy) }
    }

    fun updateSettings(settings: PostSettings) {
        _uiState.update { it.copy(settings = settings) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }





    fun searchUsers(query: String) {
        userSearchJob?.cancel()
        userSearchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(userSearchResults = emptyList()) }
                return@launch
            }

            delay(300)
            _uiState.update { it.copy(isSearchLoading = true) }

            userRepository.searchUsers(query).onSuccess { users ->
                val mappedUsers = users.map { profile ->
                    User(
                        uid = profile.uid,
                        username = profile.username,
                        displayName = profile.displayName,
                        avatar = profile.avatar,
                        verify = profile.verify
                    )
                }
                _uiState.update { it.copy(userSearchResults = mappedUsers, isSearchLoading = false) }
            }.onFailure { error ->
                android.util.Log.e("CreatePost", "User search failed", error)
                _uiState.update { it.copy(isSearchLoading = false, error = "User search failed.") }
            }
        }
    }

    fun searchLocations(query: String) {
        locationSearchJob?.cancel()
        locationSearchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(locationSearchResults = emptyList()) }
                return@launch
            }

            delay(500)
            _uiState.update { it.copy(isSearchLoading = true) }


            locationRepository.searchLocations(query)
                .onSuccess { results ->
                    _uiState.update { it.copy(locationSearchResults = results, isSearchLoading = false) }
                }
                .onFailure { error ->
                    android.util.Log.e("CreatePost", "Location search failed", error)
                    _uiState.update { it.copy(isSearchLoading = false, error = "Location search failed.") }
                }
        }
    }

    fun searchFeelings(query: String) {
        if (query.isBlank()) {
             _uiState.update { it.copy(feelingSearchResults = allFeelings) }
        } else {
             val filtered = allFeelings.filter { it.text.contains(query, ignoreCase = true) }
             _uiState.update { it.copy(feelingSearchResults = filtered) }
        }
    }

    fun clearSearchResults() {
        _uiState.update { it.copy(
            userSearchResults = emptyList(),
            locationSearchResults = emptyList(),
            feelingSearchResults = allFeelings,
            isSearchLoading = false
        )}
    }

    fun submitPost() {
        if (_uiState.value.isLoading) return

        val currentState = _uiState.value
        val text = currentState.postText.trim()


        val hasVideo = currentState.mediaItems.any { it.type == MediaType.VIDEO }
        if (hasVideo) {
            submitReel()
            return
        }

        if (text.isEmpty() && currentState.mediaItems.isEmpty() && currentState.pollData == null && currentState.youtubeUrl == null) {
            _uiState.update { it.copy(error = "Please add some content to your post") }
            return
        }


        viewModelScope.launch {
            val currentUser = authService.getCurrentUser()
            if (currentUser == null) {
                _uiState.update { it.copy(error = "Not logged in") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, uploadProgress = 0f) }

            val postKey = originalPost?.key ?: "post_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val timestamp = System.currentTimeMillis()
            val publishDate = Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val postType = when {
                currentState.mediaItems.any { it.type == MediaType.VIDEO } -> "VIDEO"
                currentState.mediaItems.isNotEmpty() -> "IMAGE"
                currentState.pollData != null -> "POLL"
                else -> "TEXT"
            }

            val pollEndTime = currentState.pollData?.let {
                Instant.ofEpochMilli(timestamp + it.durationHours * 3600 * 1000L)
                    .atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_INSTANT)
            }

            val post = Post(
                id = editPostId ?: UUID.randomUUID().toString(),
                key = postKey,
                authorUid = currentUser.id,
                postText = text.ifEmpty { null },
                postType = postType,
                postVisibility = currentState.privacy,
                postHideViewsCount = if (currentState.settings.hideViewsCount) "true" else "false",
                postHideLikeCount = if (currentState.settings.hideLikeCount) "true" else "false",
                postHideCommentsCount = if (currentState.settings.hideCommentsCount) "true" else "false",
                postDisableComments = if (currentState.settings.disableComments) "true" else "false",
                publishDate = publishDate,
                timestamp = timestamp,
                youtubeUrl = currentState.youtubeUrl,
                hasPoll = currentState.pollData != null,
                pollQuestion = currentState.pollData?.question,
                pollOptions = currentState.pollData?.options?.map { PollOption(text = it, votes = 0) },
                pollEndTime = pollEndTime,
                pollAllowMultiple = false,
                hasLocation = currentState.location != null,
                locationName = currentState.location?.name,
                locationAddress = currentState.location?.address,
                locationLatitude = currentState.location?.latitude,
                locationLongitude = currentState.location?.longitude,
                locationPlaceId = null,
                metadata = PostMetadata(
                    layoutType = DEFAULT_LAYOUT_TYPE,
                    taggedPeople = currentState.taggedPeople.ifEmpty { null },
                    feeling = currentState.feeling,
                    backgroundColor = currentState.textBackgroundColor
                )
            )


            val newMedia = currentState.mediaItems.filter { !it.url.startsWith("http") }
            val existingMedia = currentState.mediaItems.filter { it.url.startsWith("http") }

            if (newMedia.isEmpty()) {
                 val finalPost = post.copy(
                     mediaItems = existingMedia.toMutableList(),
                     postImage = existingMedia.firstOrNull { it.type == MediaType.IMAGE }?.url
                 )
                 saveOrUpdatePost(finalPost)
            } else {
                 uploadMediaAndSave(post, newMedia, existingMedia)
            }
        }
    }

    private suspend fun uploadMediaAndSave(post: Post, newMedia: List<MediaItem>, existingMedia: List<MediaItem>) {
        try {
            val totalItems = newMedia.size
            val progressMap = ConcurrentHashMap<Int, Float>()

            // Initialize progress map
            for (i in 0 until totalItems) {
                progressMap[i] = 0f
            }

            val uploadedResults = coroutineScope {
                newMedia.mapIndexed { index, mediaItem ->
                    async(Dispatchers.IO) {
                        try {
                            val filePath = mediaItem.url
                            val file = java.io.File(filePath)

                            if (!filePath.startsWith("content://") && !file.exists()) {
                                android.util.Log.e("CreatePost", "File not found: $filePath")
                                // Set progress to 100% for skipped item
                                progressMap[index] = 1.0f
                                updateTotalProgress(progressMap, totalItems)
                                return@async null
                            }

                            val sharedMediaType = when (mediaItem.type) {
                                MediaType.IMAGE -> com.synapse.social.studioasinc.shared.domain.model.MediaType.PHOTO
                                MediaType.VIDEO -> com.synapse.social.studioasinc.shared.domain.model.MediaType.VIDEO
                                else -> com.synapse.social.studioasinc.shared.domain.model.MediaType.OTHER
                            }

                            val result = uploadMediaUseCase(
                                filePath = filePath,
                                mediaType = sharedMediaType,
                                onProgress = { progress ->
                                    progressMap[index] = progress
                                    updateTotalProgress(progressMap, totalItems)
                                }
                            )

                            // If upload succeeds, return the new MediaItem
                            val uploadedUrl = result.getOrThrow()

                            // Set progress to 100%
                            progressMap[index] = 1.0f
                            updateTotalProgress(progressMap, totalItems)

                            android.util.Log.d("CreatePost", "Uploaded ${mediaItem.type}: $uploadedUrl")

                            mediaItem.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                url = uploadedUrl,
                                mimeType = getApplication<Application>().contentResolver.getType(android.net.Uri.parse(filePath))
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("CreatePost", "Media upload failed: ${e.message}", e)
                            // Propagate exception to cancel other uploads
                            throw e
                        }
                    }
                }.awaitAll()
            }

            val uploadedItems = uploadedResults.filterNotNull()
            val allMedia = existingMedia + uploadedItems
            val updatedPost = post.copy(
                mediaItems = allMedia.toMutableList(),
                postImage = allMedia.firstOrNull { it.type == MediaType.IMAGE }?.url
            )
            saveOrUpdatePost(updatedPost)

        } catch (e: Exception) {
            android.util.Log.e("CreatePost", "Upload process failed", e)
            _uiState.update { it.copy(isLoading = false, error = "Upload failed: ${e.message}") }
        }
    }

    private fun updateTotalProgress(progressMap: ConcurrentHashMap<Int, Float>, totalItems: Int) {
        val totalProgress = progressMap.values.sum() / totalItems
        _uiState.update { it.copy(uploadProgress = totalProgress) }
    }

    private fun saveOrUpdatePost(post: Post) {
        viewModelScope.launch {
            if (!_uiState.value.isEditMode && post.username.isNullOrEmpty()) {
                try {
                    userRepository.getUserById(post.authorUid).onSuccess { user ->
                        post.username = user?.username
                        post.avatarUrl = user?.avatar
                        post.isVerified = user?.verify ?: false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val result = if (_uiState.value.isEditMode) {
                 postRepository.updatePost(post)
            } else {
                 postRepository.createPost(post)
            }

            result.onSuccess {
                    clearDraft()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPostCreated = true,
                            postText = "",
                            mediaItems = emptyList(),
                            pollData = null,
                            location = null,
                            youtubeUrl = null,
                            taggedPeople = emptyList(),
                            feeling = null,
                            textBackgroundColor = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed: ${e.message}") }
                }
        }
    }

    private fun submitReel() {
        val currentState = _uiState.value

        val videoItem = currentState.mediaItems.firstOrNull { it.type == MediaType.VIDEO } ?: return
        val videoPath = videoItem.url
        val isContentUri = videoPath.startsWith("content://")
        val file = java.io.File(videoPath)

        if (!isContentUri && !file.exists()) {
            _uiState.update { it.copy(error = "Video file not found") }
            return
        }


        if (!isContentUri) {
            val dataDir = getApplication<Application>().applicationInfo.dataDir
            val isInDataDir = file.absolutePath.startsWith(dataDir)
            val isInCacheDir = file.absolutePath.startsWith(getApplication<Application>().cacheDir.absolutePath)

            if (isInDataDir && !isInCacheDir) {
                _uiState.update { it.copy(error = "Invalid video file source") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, uploadProgress = 0f) }

            val fileName = "reel_${System.currentTimeMillis()}.mp4"
            var channel: io.ktor.utils.io.ByteReadChannel?
            var size: Long

            try {
                if (isContentUri) {
                    val uri = Uri.parse(videoPath)
                    val resolver = getApplication<Application>().contentResolver
                    val inputStream = resolver.openInputStream(uri)
                        ?: throw java.io.IOException("Failed to open input stream")
                    channel = inputStream.toByteReadChannel()
                    size = resolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                } else {
                    channel = file.inputStream().toByteReadChannel()
                    size = file.length()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to prepare video: ${e.message}") }
                return@launch
            }

            if (channel == null) {
                 _uiState.update { it.copy(isLoading = false, error = "Failed to create read channel") }
                 return@launch
            }

            val metadataMap = mutableMapOf<String, Any?>()
            currentState.feeling?.let { metadataMap["feeling"] = mapOf("emoji" to it.emoji, "text" to it.text, "type" to it.type.name) }
            if (currentState.taggedPeople.isNotEmpty()) {
                metadataMap["tagged_people"] = currentState.taggedPeople.map { mapOf("uid" to it.uid, "username" to it.username) }
            }
            metadataMap["layout_type"] = DEFAULT_LAYOUT_TYPE
            currentState.textBackgroundColor?.let { metadataMap["background_color"] = it }

            reelRepository.uploadReel(
                dataChannel = channel,
                size = size,
                fileName = fileName,
                caption = currentState.postText,
                musicTrack = "Original Audio",
                locationName = currentState.location?.name,
                locationAddress = currentState.location?.address,
                locationLatitude = currentState.location?.latitude,
                locationLongitude = currentState.location?.longitude,
                metadata = metadataMap,
                onProgress = { progress ->
                    _uiState.update { it.copy(uploadProgress = progress) }
                }
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false, isPostCreated = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "Reel upload failed: ${e.message}") }
            }
        }
    }
    companion object {
        private const val DEFAULT_LAYOUT_TYPE = "COLUMNS"
    }
}
