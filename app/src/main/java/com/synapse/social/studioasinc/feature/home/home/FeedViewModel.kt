package com.synapse.social.studioasinc.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.data.repository.PostRepository
import com.synapse.social.studioasinc.data.repository.PollRepository
import com.synapse.social.studioasinc.data.repository.ReactionRepository
import com.synapse.social.studioasinc.data.local.database.AppDatabase
import com.synapse.social.studioasinc.domain.model.Post
import com.synapse.social.studioasinc.domain.model.ReactionType
import com.synapse.social.studioasinc.domain.model.User
import com.synapse.social.studioasinc.feature.shared.components.post.PostCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.synapse.social.studioasinc.core.util.ScrollPositionState
import com.synapse.social.studioasinc.feature.shared.components.post.PostEventBus
import com.synapse.social.studioasinc.feature.shared.components.post.PostEvent
import com.synapse.social.studioasinc.feature.shared.components.post.PostMapper
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.VisibleForTesting

import com.synapse.social.studioasinc.data.repository.SettingsRepository
import com.synapse.social.studioasinc.ui.settings.PostViewStyle

data class FeedUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val postViewStyle: PostViewStyle = PostViewStyle.SWIPE
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val reactionRepository: ReactionRepository,
    private val pollRepository: PollRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _modifiedPosts = MutableStateFlow<Map<String, Post>>(emptyMap())
    private val MAX_MODIFIED_POSTS = 100


    private val cachedPosts = postRepository.getPostsPaged()
        .cachedIn(viewModelScope)


    val posts: Flow<PagingData<Post>> = cachedPosts
        .combine(_modifiedPosts) { pagingData, modifications ->
            pagingData.map { post ->
                modifications[post.id] ?: post
            }
        }

    private var savedScrollPosition: ScrollPositionState? = null

    init {

        viewModelScope.launch {
            settingsRepository.appearanceSettings.collect { settings ->
                _uiState.update { it.copy(postViewStyle = settings.postViewStyle) }
            }
        }


        viewModelScope.launch {
            PostEventBus.events.collect { event ->
                when (event) {
                    is PostEvent.Liked -> {
                        val current = _modifiedPosts.value[event.postId]
















                    }
                    is PostEvent.Updated -> {
                        cacheModifiedPost(event.post)
                    }
                    is PostEvent.Deleted -> {


                    }
                    else -> {}
                }
            }
        }
    }

    private fun cacheModifiedPost(post: Post) {
        _modifiedPosts.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            newMap.remove(post.id)
            newMap[post.id] = post


            while (newMap.size > MAX_MODIFIED_POSTS) {

                val iterator = newMap.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
            newMap
        }
    }

    @VisibleForTesting
    fun getModifiedPostsCount(): Int {
        return _modifiedPosts.value.size
    }

    fun likePost(post: Post) {
        performReaction(post, ReactionType.LIKE)
    }

    fun reactToPost(post: Post, reactionType: ReactionType) {
        performReaction(post, reactionType)
    }

    private fun performReaction(post: Post, reactionType: ReactionType) {
         viewModelScope.launch {

             val currentReaction = post.userReaction


             val isRemoving = currentReaction == reactionType
             val newReaction = if (isRemoving) null else reactionType





             val countChange = when {
                 isRemoving -> -1
                 currentReaction == null -> 1
                 else -> 0
             }

             val newCount = post.likesCount + countChange


             val updatedReactions = post.reactions?.toMutableMap() ?: mutableMapOf()
             if (isRemoving) {
                 val currentCount = updatedReactions[reactionType] ?: 1
                 updatedReactions[reactionType] = maxOf(0, currentCount - 1)
             } else {

                 if (currentReaction != null) {
                     val oldTypeCount = updatedReactions[currentReaction] ?: 1
                     updatedReactions[currentReaction] = maxOf(0, oldTypeCount - 1)
                 }

                 val newTypeCount = updatedReactions[reactionType] ?: 0
                 updatedReactions[reactionType] = newTypeCount + 1
             }


             val updatedPost = post.copy(
                 likesCount = maxOf(0, newCount),
                 userReaction = newReaction,
                 reactions = updatedReactions
             )


             cacheModifiedPost(updatedPost)
             PostEventBus.emit(PostEvent.Updated(updatedPost))


             try {
                  reactionRepository.toggleReaction(post.id, "post", reactionType, currentReaction, skipCheck = true).onFailure {

                      cacheModifiedPost(post)
                      PostEventBus.emit(PostEvent.Updated(post))
                  }
             } catch (e: Exception) {

                  cacheModifiedPost(post)
                  PostEventBus.emit(PostEvent.Updated(post))
             }
         }
    }

    fun votePoll(post: Post, optionIndex: Int) {

        val currentOptions = post.pollOptions ?: return
        if (post.userPollVote != null) return

        val updatedOptions = currentOptions.mapIndexed { index, option ->
            if (index == optionIndex) option.copy(votes = option.votes + 1) else option
        }

        val updatedPost = post.copy(
            pollOptions = updatedOptions,
            userPollVote = optionIndex
        )

        cacheModifiedPost(updatedPost)
        PostEventBus.emit(PostEvent.Updated(updatedPost))

        viewModelScope.launch {
            try {
                pollRepository.submitVote(post.id, optionIndex)
            } catch (e: Exception) {
                e.printStackTrace()

                _modifiedPosts.update { it - post.id }

            }
        }
    }

    fun revokeVote(post: Post) {
        val currentVoteIndex = post.userPollVote ?: return
        val currentOptions = post.pollOptions ?: return


        val updatedOptions = currentOptions.mapIndexed { index, option ->
            if (index == currentVoteIndex) option.copy(votes = maxOf(0, option.votes - 1)) else option
        }

        val updatedPost = post.copy(
            pollOptions = updatedOptions,
            userPollVote = null
        )

        cacheModifiedPost(updatedPost)

        viewModelScope.launch {
            try {
                pollRepository.revokeVote(post.id)
            } catch (e: Exception) {
                e.printStackTrace()

                _modifiedPosts.update { it - post.id }
            }
        }
    }

    fun bookmarkPost(post: Post) {
        viewModelScope.launch {
            try {

                val client = com.synapse.social.studioasinc.core.network.SupabaseClient.client
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    client.from("bookmarks").insert(mapOf(
                        "user_id" to currentUserId,
                        "post_id" to post.id
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {


    }





    fun mapPostToState(post: Post): PostCardState {
        return PostMapper.mapToState(post)
    }

    fun saveScrollPosition(position: Int, offset: Int) {
        savedScrollPosition = ScrollPositionState(position, offset)
    }

    fun restoreScrollPosition(): ScrollPositionState? {
        val position = savedScrollPosition
        return if (position != null && !position.isExpired()) {
            position
        } else {
            savedScrollPosition = null
            null
        }
    }

    fun isPostOwner(post: Post): Boolean {
        return authRepository.getCurrentUserId() == post.authorUid
    }

    fun areCommentsDisabled(post: Post): Boolean {
        return post.postDisableComments?.toBoolean() ?: false
    }

    fun editPost(post: Post) {

    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sharePost(post: Post) {

    }

    fun copyPostLink(post: Post) {

    }

    fun toggleComments(post: Post) {
        viewModelScope.launch {
            try {
                val newState = !(post.postDisableComments?.toBoolean() ?: false)
                postRepository.updatePost(post.id, mapOf("post_disable_comments" to newState))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reportPost(post: Post) {

    }

    fun blockUser(userId: String) {

    }
}
