package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.feature.stories.tray

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.StoryWithUser
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.User

/**
 * Gradient colors for the unseen story ring
 */
private val storyGradientColors = listOf(
    Color(0xFFE040FB), // Purple
    Color(0xFFFF4081), // Pink
    Color(0xFFFF6E40), // Orange
    Color(0xFFFFAB00)  // Amber
)

/**
 * Color for seen story ring
 */
private val seenStoryRingColor = Color(0xFF424242)

/**
 * Horizontal Story Tray component displayed at the top of the feed.
 * Uses Material 3 Carousel with rectangular cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryTray(
    currentUser: User?,
    myStory: StoryWithUser?,
    friendStories: List<StoryWithUser>,
    onMyStoryClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onStoryClick: (StoryWithUser) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    if (isLoading) {
        StoryTrayShimmer(modifier)
        return
    }

    // Determine if "My Story" should be shown (active stories exist)
    val hasActiveStory = myStory != null && myStory.stories.isNotEmpty()

    // Calculate total items:
    // 1. Add Story Button (Always)
    // 2. My Story (Only if active)
    // 3. Friend Stories
    val totalItems = 1 + (if (hasActiveStory) 1 else 0) + friendStories.size
    val state = rememberCarouselState { totalItems }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalMultiBrowseCarousel(
            state = state,
            preferredItemWidth = 120.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(180.dp) // Height for 2:3 aspect ratio with 120.dp width
        ) { index ->
            if (index == 0) {
                // Add Story Item (Always first)
                StoryCard(
                    imageUrl = currentUser?.avatar,
                    avatarUrl = currentUser?.avatar,
                    title = "My Story", // Text below card. For Add button, logic below overrides this to "Create Story"
                    isAddButton = true,
                    hasUnseen = false,
                    onClick = onAddStoryClick,
                    modifier = Modifier.clip(MaterialTheme.shapes.large)
                )
            } else if (hasActiveStory && index == 1) {
                // My Story Item (Active) - Shown second if active
                val latestStory = myStory?.stories?.lastOrNull()

                StoryCard(
                    imageUrl = latestStory?.mediaUrl, // Show latest story media as background
                    avatarUrl = currentUser?.avatar,
                    title = "My Story",
                    isAddButton = false,
                    hasUnseen = false, // Assumed seen by self
                    onClick = onMyStoryClick,
                    modifier = Modifier.clip(MaterialTheme.shapes.large)
                )
            } else {
                // Friend Story Item
                // Calculate index offset based on presence of My Story item
                val friendIndex = index - (if (hasActiveStory) 2 else 1)

                if (friendIndex in friendStories.indices) {
                    val friendStory = friendStories[friendIndex]
                    val latestStory = friendStory.stories.lastOrNull()

                    StoryCard(
                        imageUrl = latestStory?.mediaUrl ?: friendStory.user.avatar,
                        avatarUrl = friendStory.user.avatar,
                        title = friendStory.user.displayName ?: friendStory.user.username ?: "User",
                        isAddButton = false,
                        hasUnseen = friendStory.hasUnseenStories,
                        onClick = { onStoryClick(friendStory) },
                        modifier = Modifier.clip(MaterialTheme.shapes.large)
                    )
                }
            }
        }

        // Subtle divider
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StoryCard(
    imageUrl: String?,
    avatarUrl: String?,
    title: String,
    isAddButton: Boolean,
    hasUnseen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = ShapeDefaults.Large,
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback background if no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.4f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.7f)
                        )
                    )
            )

            // Content Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Top Left: Avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(32.dp) // Small avatar
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (hasUnseen) {
                                    Modifier.border(
                                        width = 2.dp,
                                        brush = Brush.sweepGradient(storyGradientColors),
                                        shape = CircleShape
                                    )
                                } else if (!isAddButton) {
                                     // Seen ring
                                     Modifier.border(
                                        width = 1.dp,
                                        color = seenStoryRingColor, // Or transparent? Usually seen is subtle
                                        shape = CircleShape
                                     )
                                } else {
                                    Modifier // No border for add button avatar itself, maybe?
                                }
                            )
                            .padding(2.dp) // Spacing between ring and avatar
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface) // Fallback bg
                    ) {
                         if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                             // Initials placeholder
                             Box(
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                             ) {
                                Text(text = title.take(1), style = MaterialTheme.typography.labelSmall)
                             }
                        }
                    }
                }

                // Add Button (Center)
                if (isAddButton) {
                     Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Bottom Left: Name
                Text(
                    text = if (isAddButton) "Create Story" else title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
private fun StoryTrayShimmer(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(ShapeDefaults.Large)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}
