package com.synapse.social.studioasinc.feature.shared.components.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.synapse.social.studioasinc.ui.settings.PostViewStyle

@Composable
fun MediaContent(
    mediaUrls: List<String>,
    isVideo: Boolean,
    onMediaClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    postViewStyle: PostViewStyle = PostViewStyle.SWIPE
) {
    if (mediaUrls.isEmpty()) return

    // Track which video is currently active (player attached)
    var activeVideoIndex by remember { mutableStateOf<Int?>(null) }

    // Reset active state when composable is detached/disposed
    DisposableEffect(Unit) {
        onDispose { activeVideoIndex = null }
    }

    if (mediaUrls.size == 1) {
        val url = mediaUrls.first()
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            MediaItemContent(
                url = url,
                isVideo = isVideo,
                isActive = activeVideoIndex == 0,
                onActivate = { activeVideoIndex = 0 },
                onClick = { onMediaClick(0) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    } else if (postViewStyle == PostViewStyle.GRID) {
        PostMediaGrid(mediaUrls, onMediaClick, modifier, isVideo)
    } else {
        val pagerState = rememberPagerState(pageCount = { mediaUrls.size })

        // Deactivate video when page changes
        LaunchedEffect(pagerState.currentPage) {
            if (activeVideoIndex != null && activeVideoIndex != pagerState.currentPage) {
                activeVideoIndex = null
            }
        }

        Box(modifier = modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    MediaItemContent(
                        url = mediaUrls[page],
                        isVideo = isVideo,
                        isActive = activeVideoIndex == page,
                        onActivate = { activeVideoIndex = page },
                        onClick = { onMediaClick(page) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            if (mediaUrls.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1}/${mediaUrls.size}",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun MediaItemContent(
    url: String,
    isVideo: Boolean,
    isActive: Boolean,
    onActivate: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVideo && isActive) {
        PostVideoPlayer(
            videoUrl = url,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.clickable {
                if (isVideo) onActivate() else onClick()
            }
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Post Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            if (isVideo) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play Video",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PostMediaGrid(
    mediaUrls: List<String>,
    onMediaClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isVideo: Boolean = false
) {
    val count = mediaUrls.size
    val spacing = 2.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        when (count) {
            2 -> {
                Row(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp)) {
                    MediaGridItem(url = mediaUrls[0], isVideo = isVideo, modifier = Modifier.weight(1f).fillMaxSize(), onClick = { onMediaClick(0) })
                    Spacer(modifier = Modifier.width(spacing))
                    MediaGridItem(url = mediaUrls[1], isVideo = isVideo, modifier = Modifier.weight(1f).fillMaxSize(), onClick = { onMediaClick(1) })
                }
            }
            3 -> {
                MediaGridItem(url = mediaUrls[0], isVideo = isVideo, modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 350.dp), onClick = { onMediaClick(0) })
                Spacer(modifier = Modifier.height(spacing))
                Row(modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 200.dp)) {
                    MediaGridItem(url = mediaUrls[1], isVideo = isVideo, modifier = Modifier.weight(1f).fillMaxSize(), onClick = { onMediaClick(1) })
                    Spacer(modifier = Modifier.width(spacing))
                    MediaGridItem(url = mediaUrls[2], isVideo = isVideo, modifier = Modifier.weight(1f).fillMaxSize(), onClick = { onMediaClick(2) })
                }
            }
            else -> {
                Row(modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 250.dp)) {
                    MediaGridItem(url = mediaUrls[0], isVideo = isVideo, modifier = Modifier.weight(1f).fillMaxSize(), onClick = { onMediaClick(0) })
                    Spacer(modifier = Modifier.width(spacing))
                    MediaGridItem(url = mediaUrls[1], isVideo = isVideo, modifier = Modifier.weight(1f).fillMaxSize(), onClick = { onMediaClick(1) })
                }
                Spacer(modifier = Modifier.height(spacing))
                Row(modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 250.dp)) {
                    MediaGridItem(url = mediaUrls[2], isVideo = isVideo, modifier = Modifier.weight(1f).fillMaxSize(), onClick = { onMediaClick(2) })
                    Spacer(modifier = Modifier.width(spacing))

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        MediaGridItem(url = mediaUrls[3], isVideo = isVideo, modifier = Modifier.fillMaxSize(), onClick = { onMediaClick(3) })
                        if (count > 4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { onMediaClick(3) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${count - 3}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaGridItem(
    url: String,
    isVideo: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        AsyncImage(
            model = url,
            contentDescription = "Post Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        if (isVideo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play Video",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
    }
}
