package com.synapse.social.studioasinc.feature.shared.reels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.synapse.social.studioasinc.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.synapse.social.studioasinc.feature.shared.reels.components.HeartAnimation
import com.synapse.social.studioasinc.shared.domain.model.Reel
import com.synapse.social.studioasinc.ui.components.CircularAvatar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelItem(
    reel: Reel,
    isActive: Boolean,
    onLikeClick: () -> Unit,
    onOpposeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    onUserClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val videoViewModel: VideoPlayerViewModel = hiltViewModel(key = reel.id)
    val videoState by videoViewModel.uiState.collectAsState()
    var showControls by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    var showHeartAnimation by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val swipeThreshold = with(density) { 100.dp.toPx() }

    // Initialize player when active
    LaunchedEffect(isActive) {
        if (isActive) {
            videoViewModel.initializePlayer(reel.videoUrl)
            videoViewModel.play()
        } else {
            // When not active, we pause.
            videoViewModel.pause()
        }
    }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Release player when item is disposed
    DisposableEffect(Unit) {
        onDispose {
            videoViewModel.releasePlayer()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onLikeClick()
                        showHeartAnimation = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = {
                        videoViewModel.toggleMute()
                        showControls = true
                    },
                    onPress = {
                        val job = scope.launch {
                            delay(500)
                            isLongPressing = true
                            videoViewModel.pause()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        try {
                            awaitRelease()
                        } finally {
                            job.cancel()
                            if (isLongPressing) {
                                isLongPressing = false
                                if (isActive) videoViewModel.play()
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var offsetX = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            onUserClick()
                        } else if (offsetX < -swipeThreshold) {
                            onBackClick()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        // Video Player
        val player = videoViewModel.getPlayerInstance()
        if (isActive || player != null) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                update = { view ->
                    view.player = player
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        startY = 500f
                    )
                )
        )

        // Playback Controls Overlay
        AnimatedVisibility(
            visible = (showControls || !videoState.isPlaying) && !isLongPressing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = {
                        if (videoState.isPlaying) videoViewModel.pause() else videoViewModel.play()
                        showControls = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                ) {
                    Icon(
                        imageVector = if (videoState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (videoState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Mute Toggle (Top Right)
        if (showControls && !isLongPressing) {
            IconButton(
                onClick = {
                    videoViewModel.toggleMute()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = if (videoState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute toggle",
                    tint = Color.White
                )
            }
        }

        // Right side controls
        AnimatedVisibility(
            visible = !isLongPressing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 60.dp, end = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                    onLikeClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }) {
                    Icon(
                        imageVector = if (reel.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (reel.isLikedByCurrentUser)
                            stringResource(R.string.like_post_liked, reel.likesCount)
                        else
                            stringResource(R.string.like_post_with_count, reel.likesCount),
                        tint = if (reel.isLikedByCurrentUser) Color.Red else Color.White
                    )
                }
                Text(text = "${reel.likesCount}", color = Color.White)
                Spacer(modifier = Modifier.size(16.dp))

                IconButton(onClick = {
                    onOpposeClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }) {
                    Icon(
                        imageVector = if (reel.isOpposedByCurrentUser) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Oppose",
                        tint = if (reel.isOpposedByCurrentUser) Color.Red else Color.White
                    )
                }
                Text(text = "${reel.opposeCount}", color = Color.White)
                Spacer(modifier = Modifier.size(16.dp))

                IconButton(onClick = {
                    onCommentClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Comment,
                        contentDescription = stringResource(R.string.comment_on_post_with_count, reel.commentCount),
                        tint = Color.White
                    )
                }
                Text(text = "${reel.commentCount}", color = Color.White)
                Spacer(modifier = Modifier.size(16.dp))

                IconButton(onClick = {
                    onShareClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share_post),
                        tint = Color.White
                    )
                }
                Text(text = "${reel.shareCount}", color = Color.White)
                Spacer(modifier = Modifier.size(16.dp))

                IconButton(onClick = {
                    onMoreClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 10.dp)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = !isLongPressing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 80.dp, bottom = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularAvatar(
                            imageUrl = reel.creatorAvatarUrl,
                            contentDescription = stringResource(R.string.author_avatar),
                            size = 32.dp,
                            onClick = {
                                onUserClick()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column {
                            Text(
                                text = reel.creatorUsername ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.clickable(onClick = onUserClick)
                            )
                            reel.locationName?.let { location ->
                                Text(
                                    text = location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    reel.caption?.let { caption ->
                        if (caption.isNotEmpty()) {
                            Text(
                                text = caption,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                maxLines = 3
                            )
                        }
                    }
                    reel.musicTrack?.let { musicTrack ->
                        if (musicTrack.isNotEmpty()) {
                            Text(
                                text = "🎵 $musicTrack",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if ((showControls || isLongPressing) && videoState.duration > 0) {
                LinearProgressIndicator(
                    progress = { videoState.progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
            }
        }

        if (showHeartAnimation) {
            HeartAnimation(
                onAnimationEnd = { showHeartAnimation = false }
            )
        }
    }
}
