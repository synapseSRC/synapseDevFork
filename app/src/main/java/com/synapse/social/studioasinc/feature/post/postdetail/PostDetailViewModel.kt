package com.synapse.social.studioasinc.feature.post.postdetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.data.repository.*
import com.synapse.social.studioasinc.domain.model.CommentWithUser
import com.synapse.social.studioasinc.domain.model.Post
import com.synapse.social.studioasinc.domain.model.PostDetailItem
import com.synapse.social.studioasinc.domain.model.ReactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val post: Post? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val replyToComment: CommentWithUser? = null,
    val editingComment: CommentWithUser? = null,
    val refreshTrigger: Int = 0,
    val commentActionsLoading: Set<String> = emptySet(),
    val replies: Map<String, List<CommentWithUser>> = emptyMap(),
    val replyLoading: Set<String> = emptySet()
) {
    val postDetailItems: List<PostDetailItem>
        get() = post?.toDetailItems() ?: emptyList()
}

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postDetailRepository: PostDetailRepository,
    private val commentRepository: CommentRepository,
    private val reactionRepository: ReactionRepository,
    private val pollRepository: PollRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val reshareRepository: ReshareRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private var currentPostId: String? = null

    fun loadPost(postId: String) {
        if (currentPostId == postId) return
        currentPostId = postId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            postDetailRepository.getPostWithDetails(postId).fold(
                onSuccess = { post ->
                    _uiState.update { it.copy(post = post, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Failed to load post") }
                }
            )
            postDetailRepository.incrementViewCount(postId)
        }
    }

    fun refreshComments() {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            postDetailRepository.getPostWithDetails(postId).onSuccess { updatedPost ->
                _uiState.update { it.copy(post = updatedPost) }
                PostEventBus.emit(PostEvent.Updated(updatedPost.post))
            }
        }
    }

    fun invalidateComments() {
        _uiState.update { it.copy(refreshTrigger = it.refreshTrigger + 1) }
    }

    fun toggleReaction(reactionType: ReactionType) {
        val postId = currentPostId ?: return
        val currentPost = _uiState.value.post ?: return
        
        viewModelScope.launch {
            val currentReaction = currentPost.userReaction
            val isRemoving = currentReaction == reactionType
            val newReaction = if (isRemoving) null else reactionType
            
            val countChange = when {
                isRemoving -> -1
                currentReaction == null -> 1
                else -> 0
            }
            
            val updatedReactions = currentPost.reactionSummary.toMutableMap()
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
            
            val optimisticPost = currentPost.copy(
                post = currentPost.post.copy(
                    likesCount = maxOf(0, currentPost.post.likesCount + countChange)
                ),
                userReaction = newReaction,
                reactionSummary = updatedReactions
            )
            
            _uiState.update { it.copy(post = optimisticPost) }
            PostEventBus.emit(PostEvent.Updated(optimisticPost.post))
            
            reactionRepository.toggleReaction(postId, "post", reactionType, currentReaction, skipCheck = true).onFailure {
                _uiState.update { it.copy(post = currentPost) }
                PostEventBus.emit(PostEvent.Updated(currentPost.post))
            }
        }
    }

    fun toggleCommentReaction(commentId: String, reactionType: ReactionType) {
        val postId = currentPostId ?: return
        val currentReaction = _uiState.value.post?.userReaction

        viewModelScope.launch {
            _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }

            reactionRepository.toggleReaction(commentId, "comment", reactionType).onSuccess {
                 invalidateComments()
            }.also {
                _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading - commentId) }
            }
        }
    }

    private var isSubmittingComment = false

    fun addComment(content: String) {
        if (isSubmittingComment) return
        val postId = currentPostId ?: return
        val parentId = _uiState.value.replyToComment?.id

        isSubmittingComment = true
        viewModelScope.launch {
            commentRepository.addComment(postId, content, parentId).onSuccess {
                refreshComments()
                setReplyTo(null)
            }.also {
                isSubmittingComment = false
            }
        }
    }

    fun setReplyTo(comment: CommentWithUser?) {
        _uiState.update { it.copy(replyToComment = comment, editingComment = null) }
    }

    fun setEditingComment(comment: CommentWithUser?) {
        _uiState.update { it.copy(editingComment = comment, replyToComment = null) }
    }

    fun deleteComment(commentId: String) {
        val postId = currentPostId ?: return
        viewModelScope.launch {
             _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }
            commentRepository.deleteComment(commentId).onSuccess {
                refreshComments()
            }.also {
                _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading - commentId) }
            }
        }
    }

    fun editComment(commentId: String, content: String) {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }
            commentRepository.updateComment(commentId, content).onSuccess {
                invalidateComments()
                setEditingComment(null)
            }.also {
                _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading - commentId) }
            }
        }
    }

    fun votePoll(optionIndex: Int) {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            pollRepository.submitVote(postId, optionIndex).onSuccess {
                postDetailRepository.getPostWithDetails(postId).onSuccess { updatedPost ->
                     _uiState.update { it.copy(post = updatedPost) }
                 }
            }
        }
    }

    fun revokeVote() {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            pollRepository.revokeVote(postId).onSuccess {
                postDetailRepository.getPostWithDetails(postId).onSuccess { updatedPost ->
                     _uiState.update { it.copy(post = updatedPost) }
                 }
            }
        }
    }

    fun toggleBookmark() {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(postId, null).onSuccess {
                postDetailRepository.getPostWithDetails(postId).onSuccess { updatedPost ->
                     _uiState.update { it.copy(post = updatedPost) }
                 }
            }
        }
    }

    fun createReshare(commentary: String?) {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            reshareRepository.createReshare(postId, commentary).onSuccess {
                postDetailRepository.getPostWithDetails(postId).onSuccess { updatedPost ->
                    _uiState.update { it.copy(post = updatedPost) }
                }
            }
        }
    }

    fun reportPost(reason: String) {
        val postId = currentPostId ?: return
        viewModelScope.launch { reportRepository.createReport(postId, reason, null) }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postDetailRepository.deletePost(postId)
        }
    }

    fun toggleComments() {

    }

    fun blockUser(userId: String) {
        viewModelScope.launch {

        }
    }

    fun hideComment(commentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }
            commentRepository.hideComment(commentId).onSuccess {
                invalidateComments()
            }.also {
                _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading - commentId) }
            }
        }
    }

    fun pinComment(commentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }
            commentRepository.pinComment(commentId).onSuccess {
                invalidateComments()
            }.also {
                _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading - commentId) }
            }
        }
    }

    fun reportComment(commentId: String, reason: String, description: String?) {
        viewModelScope.launch {
            commentRepository.reportComment(commentId, reason)
        }
    }

    fun copyLink(postId: String, context: Context) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        val clip = ClipData.newPlainText("Post Link", "synapse://post/$postId")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
    }

    fun loadReplies(commentId: String) {
        if (_uiState.value.replyLoading.contains(commentId)) return

        viewModelScope.launch {
            _uiState.update { it.copy(replyLoading = it.replyLoading + commentId) }
            commentRepository.getReplies(commentId).fold(
                onSuccess = { replies ->
                    _uiState.update {
                        it.copy(
                            replies = it.replies + (commentId to replies),
                            replyLoading = it.replyLoading - commentId
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(replyLoading = it.replyLoading - commentId) }
                }
            )
        }
    }
}
