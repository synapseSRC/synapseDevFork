package com.synapse.social.studioasinc.feature.post.postdetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.synapse.social.studioasinc.data.repository.*
import com.synapse.social.studioasinc.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postDetailRepository: PostDetailRepository,
    private val commentRepository: CommentRepository,
    private val reactionRepository: ReactionRepository,
    private val pollRepository: PollRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val reshareRepository: ReshareRepository,
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val client: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _commentsPagingFlow = MutableStateFlow<Flow<PagingData<CommentWithUser>>>(emptyFlow())
    val commentsPagingFlow: StateFlow<Flow<PagingData<CommentWithUser>>> = _commentsPagingFlow.asStateFlow()

    private var currentPostId: String? = null

    init {
        val currentUser = client.auth.currentUserOrNull()
        _uiState.update { it.copy(currentUserId = currentUser?.id) }
    }

    fun loadPost(postId: String) {

        if (currentPostId != postId) {
            currentPostId = postId
            val flow = Pager(
                PagingConfig(pageSize = 20)
            ) {
                 CommentPagingSource(commentRepository, postId)
            }.flow.cachedIn(viewModelScope)

            _commentsPagingFlow.value = flow
        }

        viewModelScope.launch {

            if (_uiState.value.post == null) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            postDetailRepository.getPostWithDetails(postId).fold(
                onSuccess = { postDetail ->
                    _uiState.update { it.copy(isLoading = false, post = postDetail) }
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
        val currentReaction = _uiState.value.post?.userReaction
        viewModelScope.launch {
            postDetailRepository.getPostWithDetails(postId).onSuccess { updatedPost ->
                _uiState.update { it.copy(post = updatedPost) }
            }
        }
    }

    fun invalidateComments() {
        _uiState.update { it.copy(refreshTrigger = it.refreshTrigger + 1) }
    }

    fun toggleReaction(reactionType: ReactionType) {
        val postId = currentPostId ?: return
        val currentReaction = _uiState.value.post?.userReaction
        viewModelScope.launch {
            reactionRepository.toggleReaction(postId, "post", reactionType, currentReaction, skipCheck = true).onSuccess {
                 postDetailRepository.getPostWithDetails(postId).onSuccess { updatedPost ->
                     _uiState.update { it.copy(post = updatedPost) }
                 }
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
        val currentReaction = _uiState.value.post?.userReaction
        val parentId = _uiState.value.replyToComment?.id

        isSubmittingComment = true
        viewModelScope.launch {
            commentRepository.createComment(postId, content, null, parentId).onSuccess {
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
        val currentReaction = _uiState.value.post?.userReaction
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
        val currentReaction = _uiState.value.post?.userReaction
        viewModelScope.launch {
            _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }
            commentRepository.editComment(commentId, content).onSuccess {
                invalidateComments()
                setEditingComment(null)
            }.also {
                _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading - commentId) }
            }
        }
    }

    fun votePoll(optionIndex: Int) {
        val postId = currentPostId ?: return
        val currentReaction = _uiState.value.post?.userReaction
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
        val currentReaction = _uiState.value.post?.userReaction
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
        val currentReaction = _uiState.value.post?.userReaction
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
        val currentReaction = _uiState.value.post?.userReaction
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
        val currentReaction = _uiState.value.post?.userReaction
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
        val postId = currentPostId ?: return
        val currentReaction = _uiState.value.post?.userReaction
        viewModelScope.launch {
            _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }
            commentRepository.hideComment(commentId).onSuccess {
                invalidateComments()
            }.also {
                _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading - commentId) }
            }
        }
    }

    fun pinComment(commentId: String, postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(commentActionsLoading = it.commentActionsLoading + commentId) }
            commentRepository.pinComment(commentId, postId).onSuccess {
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
