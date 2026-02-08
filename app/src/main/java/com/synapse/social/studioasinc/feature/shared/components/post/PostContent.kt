package com.synapse.social.studioasinc.feature.shared.components.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.synapse.social.studioasinc.ui.settings.PostViewStyle

@Composable
fun PostContent(
    text: String?,
    mediaUrls: List<String>,
    postViewStyle: PostViewStyle = PostViewStyle.SWIPE,
    isVideo: Boolean,
    pollQuestion: String?,
    pollOptions: List<PollOption>?,
    onMediaClick: (Int) -> Unit,
    onPollVote: (String) -> Unit,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (!text.isNullOrBlank()) {
            var localExpanded by remember { mutableStateOf(false) }
            val showFullText = isExpanded || localExpanded

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                maxLines = if (showFullText) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow && !showFullText) {

                    }
                }
            )
        }

        if (mediaUrls.isNotEmpty()) {
            MediaContent(
                mediaUrls = mediaUrls,
                isVideo = isVideo,
                postViewStyle = postViewStyle,
                onMediaClick = onMediaClick,
                modifier = Modifier.padding(horizontal = 0.dp)
            )
        }

        if (pollQuestion != null && pollOptions != null) {
            PollContent(
                question = pollQuestion,
                options = pollOptions,
                totalVotes = pollOptions.sumOf { it.voteCount },
                onVote = onPollVote
            )
        }
    }
}
