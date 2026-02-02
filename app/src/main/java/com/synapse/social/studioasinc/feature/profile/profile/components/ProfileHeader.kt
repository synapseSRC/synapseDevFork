package com.synapse.social.studioasinc.ui.profile.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.synapse.social.studioasinc.domain.model.UserStatus
import com.synapse.social.studioasinc.R
import com.synapse.social.studioasinc.feature.shared.components.ExpressiveButton
import com.synapse.social.studioasinc.feature.shared.components.ButtonVariant

/**
 * Enhanced Profile Header with cover photo, animated stats, and modern design.
 *
 * Features:
 * - Cover photo with parallax scrolling
 * - Profile image with story ring animation
 * - Animated stat counters
 * - Expandable bio with smooth animation
 * - Premium action buttons with icons
 * - Verified badge with subtle animation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(
    avatar: String?,
    status: UserStatus? = null,
    coverImageUrl: String? = null,
    name: String?,
    username: String,
    nickname: String?,
    bio: String?,
    isVerified: Boolean,
    hasStory: Boolean,
    postsCount: Int,
    followersCount: Int,
    followingCount: Int,
    isOwnProfile: Boolean,
    isFollowing: Boolean = false,
    isFollowLoading: Boolean = false,
    scrollOffset: Float = 0f,
    onProfileImageClick: () -> Unit,
    onCoverPhotoClick: () -> Unit = {},
    onEditProfileClick: () -> Unit,
    onFollowClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onAddStoryClick: () -> Unit,
    onMoreClick: () -> Unit,
    onStatsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var bioExpanded by remember { mutableStateOf(false) }

    // Entry animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 200),
        label = "contentAlpha"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Cover Photo with Profile Image and Name/Stats Overlay
        Box(modifier = Modifier.fillMaxWidth()) {
            CoverPhoto(
                coverImageUrl = coverImageUrl,
                scrollOffset = scrollOffset,
                isOwnProfile = isOwnProfile,
                onEditClick = onCoverPhotoClick,
                height = 200.dp
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = 96.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileImageWithRing(
                    avatar = avatar,
                    size = 120.dp,
                    status = status,
                    hasStory = hasStory,
                    isOwnProfile = isOwnProfile,
                    onClick = onProfileImageClick
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name ?: username,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Account Switcher Placeholder
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { /* Placeholder */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Switch account",
                                modifier = Modifier.size(20.dp)
                            )
                            // Red dot notification badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .semantics {
                                        contentDescription = "New notifications"
                                    }
                            )
                        }
                    }

                    StatsRow(
                        postsCount = postsCount,
                        followersCount = followersCount,
                        followingCount = followingCount,
                        onStatsClick = onStatsClick,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(112.dp))

        // Content below (Bio, Mutuals, Actions)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer { alpha = contentAlpha }
        ) {
            // Bio
            if (!bio.isNullOrBlank()) {
                ExpandableBio(
                    bio = bio,
                    expanded = bioExpanded,
                    onToggle = { bioExpanded = !bioExpanded }
                )
            } else if (isOwnProfile) {
                Text(
                    text = stringResource(id = R.string.add_bio_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { onEditProfileClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mutual Followers Mock
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .padding(start = (i * 20).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when(i) {
                                        0 -> Color(0xFFB0BEC5)
                                        1 -> Color(0xFF90A4AE)
                                        else -> Color(0xFF78909C)
                                    }
                                )
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(76.dp))
                Text(
                    text = "Followers with things in common",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            ProfileActionButtons(
                isOwnProfile = isOwnProfile,
                isFollowing = isFollowing,
                isFollowLoading = isFollowLoading,
                onEditProfileClick = onEditProfileClick,
                onAddStoryClick = onAddStoryClick,
                onFollowClick = onFollowClick,
                onMessageClick = onMessageClick,
                onMoreClick = onMoreClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


/**
 * Animated verified badge with subtle pulse effect.
 */
@Composable
fun AnimatedVerifiedBadge(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "verifiedBadge")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "verifiedScale"
    )

    Icon(
        imageVector = Icons.Default.Verified,
        contentDescription = "Verified",
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .size(22.dp)
            .scale(scale)
    )
}

/**
 * Expandable bio text with smooth animation.
 */
@Composable
private fun ExpandableBio(
    bio: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldCollapse = bio.length > 150

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                (fadeIn(animationSpec = tween(200)) + expandVertically())
                    .togetherWith(fadeOut(animationSpec = tween(200)) + shrinkVertically())
            },
            label = "bioExpand"
        ) { isExpanded ->
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyLarge, // MD3: Body large for bio
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2, // MD3: Better line height
                maxLines = if (isExpanded || !shouldCollapse) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = shouldCollapse) { onToggle() }
            )
        }

        if (shouldCollapse) {
            Spacer(modifier = Modifier.height(8.dp)) // MD3: 8dp spacing
            Text(
                text = if (expanded) "Show less" else "See more",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium, // MD3: Medium weight for actions
                modifier = Modifier.clickable { onToggle() }
            )
        }
    }
}

/**
 * Action buttons row (Edit Profile / Follow / Message).
 */
@Composable
private fun ProfileActionButtons(
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    onEditProfileClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isOwnProfile) {
            // Add Story Button
            ExpressiveButton(
                onClick = onAddStoryClick,
                modifier = Modifier.weight(1f),
                text = "Add Story",
                variant = ButtonVariant.Filled,
                icon = Icons.Default.Add
            )

            // Edit Profile Button
            ExpressiveButton(
                onClick = onEditProfileClick,
                modifier = Modifier.weight(1f),
                text = "Edit Profile",
                variant = ButtonVariant.FilledTonal
            )

            // More Button
            Surface(
                onClick = onMoreClick,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More options"
                    )
                }
            }
        } else {
            // Follow/Following Button
            AnimatedFollowButton(
                isFollowing = isFollowing,
                isLoading = isFollowLoading,
                onClick = onFollowClick,
                modifier = Modifier.weight(1f)
            )

            // Message Button (Disabled)
            ExpressiveButton(
                onClick = { /* Disabled */ },
                modifier = Modifier.weight(1f),
                text = "Message",
                variant = ButtonVariant.Outlined,
                enabled = false
            )
        }
    }
}

/**
 * Animated follow button with state transition.
 */
@Composable
fun AnimatedFollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    // Material 3 Expressive colors
    // When following (active state): primary container
    // When not following (action needed): inverse primary or tertiary

    val containerColor by animateColorAsState(
        targetValue = if (isFollowing) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "followButtonColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isFollowing) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onPrimary
        },
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "followButtonContentColor"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.height(44.dp),
        enabled = !isLoading,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), // Check M3 Expressive specs (usually larger corner radius for expressive buttons)
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.7f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
             if (isLoading) {
                 CircularProgressIndicator(
                     modifier = Modifier.size(18.dp),
                     strokeWidth = 2.dp,
                     color = contentColor
                 )
             } else {
                 AnimatedContent(
                    targetState = isFollowing,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f))
                            .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f))
                    },
                    label = "followButtonContent"
                ) { following ->
                    Text(
                        text = if (following) "Following" else "Follow",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
             }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileHeaderPreview() {
    MaterialTheme {
        ProfileHeader(
            avatar = null,
            status = UserStatus.ONLINE,
            coverImageUrl = null,
            name = "John Doe",
            username = "johndoe",
            nickname = "JD",
            bio = "Software developer | Tech enthusiast | Coffee lover ☕️ | Building amazing things with code every day. Let's connect and create something awesome together!",
            isVerified = true,
            hasStory = true,
            postsCount = 142,
            followersCount = 12345,
            followingCount = 567,
            isOwnProfile = true,
            onProfileImageClick = {},
            onEditProfileClick = {},
            onAddStoryClick = {},
            onMoreClick = {},
            onStatsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileHeaderOtherUserPreview() {
    MaterialTheme {
        ProfileHeader(
            avatar = null,
            status = UserStatus.OFFLINE,
            coverImageUrl = null,
            name = "Jane Smith",
            username = "janesmith",
            nickname = null,
            bio = "Digital artist 🎨 | Dreamer",
            isVerified = false,
            hasStory = false,
            postsCount = 42,
            followersCount = 1234,
            followingCount = 234,
            isOwnProfile = false,
            isFollowing = false,
            onProfileImageClick = {},
            onEditProfileClick = {},
            onFollowClick = {},
            onMessageClick = {},
            onAddStoryClick = {},
            onMoreClick = {},
            onStatsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatedFollowButtonPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedFollowButton(
                isFollowing = false,
                isLoading = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            AnimatedFollowButton(
                isFollowing = true,
                isLoading = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}
