package com.synapse.social.studioasinc.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.synapse.social.studioasinc.R
import com.synapse.social.studioasinc.ui.navigation.HomeDestinations
import com.synapse.social.studioasinc.ui.navigation.HomeNavGraph
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import com.synapse.social.studioasinc.feature.shared.reels.ReelUploadManager
import com.synapse.social.studioasinc.feature.shared.reels.components.UploadProgressOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    reelUploadManager: ReelUploadManager,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToCreatePost: (String?) -> Unit,
    onNavigateToStoryViewer: (String) -> Unit,
    onNavigateToCreateReel: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isPostDetail = currentDestination?.route == HomeDestinations.PostDetail.route

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Control bottom nav visibility based on scroll
    val isBottomBarVisible = (scrollBehavior.state.collapsedFraction < 0.5f) && !isPostDetail

    val navBarTranslationY by animateFloatAsState(
        targetValue = if (isBottomBarVisible) 0f else 1f, // 0f = visible, 1f = hidden
        label = "NavBarAnimation"
    )

    // Fetch user profile logic
    val currentUser = com.synapse.social.studioasinc.core.network.SupabaseClient.client.auth.currentUserOrNull()
    var userAvatarUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val result = com.synapse.social.studioasinc.core.network.SupabaseClient.client.from("users")
                    .select(columns = Columns.raw("avatar")) {
                        filter {
                            eq("uid", currentUser.id)
                        }
                    }.decodeSingleOrNull<JsonObject>()

                userAvatarUrl = result?.get("avatar")?.toString()?.replace("\"", "")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (!isPostDetail) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                            )
                        },
                        actions = {
                            // Primary action - Create Post (most prominent)
                            androidx.compose.material3.FilledTonalIconButton(
                                onClick = { onNavigateToCreatePost(null) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddBox,
                                    contentDescription = stringResource(R.string.create_post)
                                )
                            }

                            // Secondary actions - smaller, less prominent
                            IconButton(onClick = onNavigateToSearch) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search)
                                )
                            }
                            IconButton(onClick = onNavigateToInbox) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.inbox)
                                )
                            }

                            // Profile - tertiary action with proper spacing
                            if (userAvatarUrl != null) {
                                com.synapse.social.studioasinc.ui.components.CircularAvatar(
                                    imageUrl = userAvatarUrl,
                                    contentDescription = stringResource(R.string.profile),
                                    size = 28.dp,
                                    modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                                    onClick = { onNavigateToProfile("me") }
                                )
                            } else {
                                IconButton(
                                    onClick = { onNavigateToProfile("me") },
                                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = stringResource(R.string.profile)
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        ) { innerPadding ->
            HomeNavGraph(
                navController = navController,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToEditPost = { postId -> onNavigateToCreatePost(postId) },
                onNavigateToStoryViewer = onNavigateToStoryViewer,
                onNavigateToCreateReel = onNavigateToCreateReel,
                modifier = Modifier.padding(innerPadding),
                bottomPadding = 80.dp
            )
        }

        // Navigation Bar overlay
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = navBarTranslationY * size.height
                }
        ) {
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == HomeDestinations.Feed.route } == true,
                onClick = {
                    navController.navigate(HomeDestinations.Feed.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentDestination?.hierarchy?.any { it.route == HomeDestinations.Feed.route } == true) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = stringResource(R.string.home)
                    )
                },
                label = { Text(stringResource(R.string.home)) }
            )

            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == HomeDestinations.Reels.route } == true,
                onClick = {
                    navController.navigate(HomeDestinations.Reels.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentDestination?.hierarchy?.any { it.route == HomeDestinations.Reels.route } == true) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle,
                        contentDescription = stringResource(R.string.reels)
                    )
                },
                label = { Text(stringResource(R.string.reels)) }
            )

            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == HomeDestinations.Notifications.route } == true,
                onClick = {
                    navController.navigate(HomeDestinations.Notifications.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    BadgedBox(
                        badge = { }
                    ) {
                        Icon(
                            imageVector = if (currentDestination?.hierarchy?.any { it.route == HomeDestinations.Notifications.route } == true) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                            contentDescription = stringResource(R.string.notifications)
                        )
                    }
                },
                label = { Text(stringResource(R.string.notifications)) }
            )
        }

        // Upload Progress Overlay
        UploadProgressOverlay(
            uploadManager = reelUploadManager,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        )
    }
}
