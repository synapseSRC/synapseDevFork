package com.synapse.social.studioasinc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.synapse.social.studioasinc.core.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import com.synapse.social.studioasinc.feature.auth.ui.AuthScreen
import com.synapse.social.studioasinc.ui.home.HomeScreen
import com.synapse.social.studioasinc.feature.profile.profile.ProfileScreen
import com.synapse.social.studioasinc.ui.inbox.InboxScreen
import com.synapse.social.studioasinc.ui.search.SearchScreen
import com.synapse.social.studioasinc.ui.search.SearchViewModel
import com.synapse.social.studioasinc.feature.post.postdetail.PostDetailScreen
import com.synapse.social.studioasinc.ui.createpost.CreatePostScreen
import com.synapse.social.studioasinc.ui.createpost.CreatePostViewModel
import com.synapse.social.studioasinc.ui.settings.SettingsScreen
import com.synapse.social.studioasinc.presentation.editprofile.EditProfileScreen
import com.synapse.social.studioasinc.presentation.editprofile.EditProfileViewModel
import com.synapse.social.studioasinc.presentation.editprofile.EditProfileEvent
import com.synapse.social.studioasinc.feature.profile.editprofile.RegionSelectionScreen
import com.synapse.social.studioasinc.presentation.editprofile.photohistory.PhotoHistoryScreen
import com.synapse.social.studioasinc.presentation.editprofile.photohistory.PhotoType
import com.synapse.social.studioasinc.feature.shared.components.compose.FollowListScreen
import com.synapse.social.studioasinc.feature.stories.viewer.StoryViewerScreen
import com.synapse.social.studioasinc.feature.stories.viewer.StoryViewerViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = AppDestination.Auth.route,
    reelUploadManager: com.synapse.social.studioasinc.feature.shared.reels.ReelUploadManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(AppDestination.Auth.route) {
            val viewModel: com.synapse.social.studioasinc.feature.auth.presentation.viewmodel.AuthViewModel = hiltViewModel()
            AuthScreen(
                viewModel = viewModel,
                onNavigateToMain = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Auth.route) { inclusive = true }
                    }
                }
            )
        }


        composable(AppDestination.Home.route) {
            HomeScreen(
                reelUploadManager = reelUploadManager,
                onNavigateToSearch = {
                    navController.navigate(AppDestination.Search.route)
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestination.Profile.createRoute(userId))
                },
                onNavigateToInbox = {
                    try {
                        navController.navigate(AppDestination.Inbox.route)
                    } catch (e: IllegalArgumentException) {

                    }
                },
                onNavigateToCreatePost = { postId ->
                    navController.navigate(AppDestination.CreatePost.createRoute(postId))
                },
                onNavigateToStoryViewer = { userId ->
                    navController.navigate(AppDestination.StoryViewer.createRoute(userId))
                },
                onNavigateToCreateReel = {
                    navController.navigate(AppDestination.CreatePost.createRoute(type = "reel"))
                }
            )
        }


        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@composable
            val targetUserId = if (userId == "me") currentUserId else userId
            val viewModel: com.synapse.social.studioasinc.feature.profile.profile.ProfileViewModel = hiltViewModel()
            ProfileScreen(
                userId = targetUserId,
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = {
                    navController.navigate(AppDestination.EditProfile.route)
                },
                onNavigateToEditPost = { postId ->
                    navController.navigate(AppDestination.CreatePost.createRoute(postId))
                },
                onNavigateToSettings = {
                    navController.navigate(AppDestination.Settings.route)
                },
                onNavigateToChat = { targetUserId ->
                    navController.navigate(AppDestination.Chat.createRoute(chatId = "new", userId = targetUserId))
                },
                onNavigateToFollowers = {
                    navController.navigate(AppDestination.FollowList.createRoute(userId, "followers"))
                },
                onNavigateToFollowing = {
                    navController.navigate(AppDestination.FollowList.createRoute(userId, "following"))
                },
                viewModel = viewModel
            )
        }

        composable(AppDestination.Inbox.route) {
            InboxScreen(
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestination.Profile.createRoute(userId))
                }
            )
        }

        composable(AppDestination.Search.route) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                viewModel = viewModel,
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestination.Profile.createRoute(userId))
                },
                onNavigateToPost = { postId ->
                    navController.navigate(AppDestination.PostDetail.createRoute(postId))
                },
                onBack = { navController.popBackStack() }
            )
        }


        composable(
            route = "post_detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestination.Profile.createRoute(userId))
                },
                onNavigateToEditPost = { pid ->
                    navController.navigate(AppDestination.CreatePost.createRoute(pid))
                }
            )
        }


        composable(
            route = AppDestination.CreatePost.route,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "post"
                }
            )
        ) { backStackEntry ->
            val viewModel: CreatePostViewModel = hiltViewModel()
            val postId = backStackEntry.arguments?.getString("postId")
            val type = backStackEntry.arguments?.getString("type") ?: "post"

            LaunchedEffect(postId, type) {
                viewModel.setCompositionType(type)
                if (postId != null) {
                    viewModel.loadPostForEdit(postId)
                }
            }

            CreatePostScreen(
                viewModel = viewModel,
                onNavigateUp = { navController.popBackStack() }
            )
        }


        composable(AppDestination.Settings.route) {
            com.synapse.social.studioasinc.ui.settings.SettingsNavHost(
                onBackClick = { navController.popBackStack() },
                onNavigateToProfileEdit = {
                    navController.navigate(AppDestination.EditProfile.route)
                },
                onNavigateToChatPrivacy = {
                    navController.navigate("chat_privacy")
                },
                onLogout = {

                    navController.navigate(AppDestination.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }


        composable(AppDestination.EditProfile.route) {
            val viewModel: EditProfileViewModel = hiltViewModel()

            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
            val selectedRegion = savedStateHandle?.get<String>("selected_region")

            LaunchedEffect(selectedRegion) {
                selectedRegion?.let { region ->
                    viewModel.onEvent(EditProfileEvent.RegionSelected(region))
                    savedStateHandle.remove<String>("selected_region")
                }
            }

            EditProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegionSelection = { _ ->
                    navController.navigate(AppDestination.RegionSelection.route)
                },
                onNavigateToPhotoHistory = { type ->
                    navController.navigate(AppDestination.PhotoHistory.createRoute(type))
                }
            )
        }


        composable(
            route = AppDestination.PhotoHistory.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: return@composable
            val photoType = try {
                PhotoType.valueOf(typeStr)
            } catch (e: IllegalArgumentException) {
                return@composable
            }

            PhotoHistoryScreen(
                type = photoType,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable(AppDestination.RegionSelection.route) {
            RegionSelectionScreen(
                onBackClick = { navController.popBackStack() },
                onRegionSelected = { region ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_region", region)
                    navController.popBackStack()
                }
            )
        }


        composable(
            route = "follow_list/{userId}/{type}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@composable
            val targetUserId = if (userId == "me") currentUserId else userId
            val listType = backStackEntry.arguments?.getString("type") ?: return@composable
            FollowListScreen(
                userId = targetUserId,
                listType = listType,
                onNavigateBack = { navController.popBackStack() },
                onUserClick = { profileUserId ->
                    navController.navigate(AppDestination.Profile.createRoute(profileUserId))
                },
                onMessageClick = { chatId ->
                    navController.navigate(AppDestination.Chat.createRoute(chatId))
                }
            )
        }


        composable(
            route = AppDestination.StoryViewer.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val viewModel: StoryViewerViewModel = hiltViewModel()

            LaunchedEffect(userId) {
                viewModel.loadStories(userId)
            }

            StoryViewerScreen(
                onClose = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

    }
}
