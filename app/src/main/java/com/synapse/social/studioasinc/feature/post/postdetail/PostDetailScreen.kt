package com.synapse.social.studioasinc.feature.post.postdetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.synapse.social.studioasinc.domain.model.CommentAction
import com.synapse.social.studioasinc.domain.model.CommentWithUser
import com.synapse.social.studioasinc.domain.model.ReactionType
import com.synapse.social.studioasinc.feature.post.postdetail.components.*
import com.synapse.social.studioasinc.feature.shared.components.MediaViewer
import com.synapse.social.studioasinc.feature.shared.components.PostOptionsBottomSheet
import com.synapse.social.studioasinc.feature.shared.components.ReportPostDialog
import com.synapse.social.studioasinc.feature.shared.components.post.PostActions
import com.synapse.social.studioasinc.feature.shared.components.post.ReactionPicker
import com.synapse.social.studioasinc.feature.shared.components.post.SharedPostItem
import com.synapse.social.studioasinc.ui.components.ExpressiveLoadingIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToEditPost: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.commentsFlow.collectAsLazyPagingItems()
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaIndex by remember { mutableIntStateOf(0) }
    var showPostOptions by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Restored state variables with correct types
    var showCommentOptions by remember { mutableStateOf<CommentWithUser?>(null) }
    var showReactionPickerForComment by remember { mutableStateOf<CommentWithUser?>(null) }
    var showReactionPicker by remember { mutableStateOf(false) }

    val currentUserId = uiState.currentUserId

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    // Helper functions for actions
    fun sharePost() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this post on Synapse: synapse://post/$postId")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
    }

    fun copyLink() {
        viewModel.copyLink(postId, context)
    }

    if (showMediaViewer && uiState.post != null) {
        val mediaUrls = uiState.post!!.post.mediaItems?.map { it.url }
            ?: listOfNotNull(uiState.post!!.post.postImage)

        MediaViewer(
            mediaUrls = mediaUrls,
            initialPage = selectedMediaIndex,
            onDismiss = { showMediaViewer = false }
        )
    }

    // Reaction Picker for Post
    if (showReactionPicker) {
        ReactionPicker(
            onReactionSelected = { reaction ->
                viewModel.toggleReaction(reaction)
                showReactionPicker = false
            },
            onDismiss = { showReactionPicker = false }
        )
    }

    // Reaction Picker for Comment
    if (showReactionPickerForComment != null) {
        ReactionPicker(
            onReactionSelected = { reaction ->
                viewModel.toggleCommentReaction(showReactionPickerForComment!!.id, reaction)
                showReactionPickerForComment = null
            },
            onDismiss = { showReactionPickerForComment = null }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        ReportPostDialog(
            onDismiss = { showReportDialog = false },
            onReport = { reason ->
                viewModel.reportPost(reason)
                showReportDialog = false
                Toast.makeText(context, "Post reported", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Post Options
    if (showPostOptions && uiState.post != null) {
        PostOptionsBottomSheet(
            post = uiState.post!!.post,
            isOwner = uiState.post!!.post.authorUid == currentUserId, // Checked ownership logic
            commentsDisabled = uiState.post!!.post.postDisableComments == "true",
            onDismiss = { showPostOptions = false },
            onEdit = {
                showPostOptions = false
                onNavigateToEditPost(uiState.post!!.post.id)
            },
            onDelete = {
                viewModel.deletePost(uiState.post!!.post.id) // Pass ID
                onNavigateBack()
            },
            onShare = {
                showPostOptions = false
                sharePost()
            },
            onCopyLink = {
                showPostOptions = false
                copyLink()
            },
            onBookmark = { viewModel.toggleBookmark() },
            onToggleComments = { viewModel.toggleComments() },
            onReport = {
                showPostOptions = false
                showReportDialog = true
            },
            onBlock = { viewModel.blockUser(uiState.post!!.post.authorUid) }, // Correct method name
            onRevokeVote = { viewModel.revokeVote() }
        )
    }

    // Comment Options
    if (showCommentOptions != null) {
        val comment = showCommentOptions!!
        CommentOptionsBottomSheet(
            comment = comment,
            isOwnComment = comment.userId == currentUserId,
            isPostAuthor = uiState.post?.post?.authorUid == currentUserId,
            onDismiss = { showCommentOptions = null },
            onAction = { action ->
                when (action) {
                    is CommentAction.Reply -> {
                        viewModel.setReplyTo(comment)
                        scope.launch {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }
                    is CommentAction.Delete -> viewModel.deleteComment(action.commentId)
                    is CommentAction.Edit -> viewModel.setEditingComment(comment)
                    is CommentAction.Report -> viewModel.reportComment(action.comment.id, "Inappropriate", null) // Corrected signature
                    is CommentAction.Copy -> {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Comment", action.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Comment copied", Toast.LENGTH_SHORT).show()
                    }
                }
                showCommentOptions = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (uiState.isLoading && uiState.post == null) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 ExpressiveLoadingIndicator()
             }
        } else if (uiState.error != null && uiState.post == null) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("Error: ${uiState.error}")
             }
        } else {
             val postDetail = uiState.post
             if (postDetail != null) {
                 CommentsList(
                     comments = pagingItems,
                     repliesState = uiState.replies,
                     replyLoadingState = uiState.replyLoading,
                     commentActionsLoading = uiState.commentActionsLoading,
                     onReplyClick = {
                         viewModel.setReplyTo(it)
                         scope.launch {
                             focusRequester.requestFocus()
                             keyboardController?.show()
                         }
                     },
                     onLikeClick = { viewModel.toggleCommentReaction(it, ReactionType.LIKE) },
                     onShowReactions = { showReactionPickerForComment = it },
                     onShowOptions = { showCommentOptions = it },
                     onUserClick = onNavigateToProfile,
                     onViewReplies = { commentId: String -> viewModel.loadReplies(commentId) },
                     modifier = Modifier.fillMaxSize(),
                     headerContent = {
                        val mergedPost = remember(postDetail) {
                             postDetail.post.copy(
                                 userReaction = postDetail.userReaction,
                                 reactions = postDetail.reactionSummary,
                                 likesCount = postDetail.reactionSummary.values.sum(),
                                 commentsCount = postDetail.post.commentsCount
                             )
                        }

                        // Map detail view model actions to shared PostActions
                        val actions = remember(viewModel, context, postId) {
                             PostActions(
                                 onLike = { viewModel.toggleReaction(ReactionType.LIKE) },
                                 onComment = {
                                     scope.launch {
                                         focusRequester.requestFocus()
                                         keyboardController?.show()
                                     }
                                 },
                                 onShare = { sharePost() },
                                 onBookmark = { viewModel.toggleBookmark() },
                                 onOptionClick = { showPostOptions = true },
                                 onPollVote = { _, index -> viewModel.votePoll(index) },
                                 onUserClick = { uid -> onNavigateToProfile(uid) },
                                 onMediaClick = { index ->
                                     selectedMediaIndex = index
                                     showMediaViewer = true
                                 }
                             )
                        }

                        SharedPostItem(
                            post = mergedPost,
                            actions = actions,
                            isExpanded = true, // Force full text display
                            modifier = Modifier.fillMaxWidth()
                        )
                     }
                 )
             }
        }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Column(
                 modifier = Modifier
                     .align(Alignment.BottomCenter)
                     .padding(bottom = 16.dp)
            ) {
                 if (uiState.replyToComment != null) {
                     ReplyIndicator(
                         replyTo = uiState.replyToComment!!,
                         onCancelReply = { viewModel.setReplyTo(null) }
                     )
                 }
                 if (uiState.editingComment != null) {
                     EditIndicator(
                         comment = uiState.editingComment!!,
                         onCancel = { viewModel.setEditingComment(null) }
                     )
                 }
                 CommentInput(
                     onSend = {
                         if (uiState.editingComment != null) {
                             viewModel.editComment(uiState.editingComment!!.id, it)
                         } else {
                             viewModel.addComment(it)
                         }
                     },
                     initialValue = uiState.editingComment?.content ?: "",
                     focusRequester = focusRequester
                 )
            }
        }
    }
}
