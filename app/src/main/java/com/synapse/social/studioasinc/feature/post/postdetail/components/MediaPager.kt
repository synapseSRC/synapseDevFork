package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.feature.post.postdetail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.feature.shared.components.post.MediaContent

@Composable
fun MediaPager(
    mediaUrls: List<String>,
    isVideo: Boolean,
    onMediaClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    MediaContent(
        mediaUrls = mediaUrls,
        isVideo = isVideo,
        onMediaClick = onMediaClick,
        modifier = modifier
    )
}
