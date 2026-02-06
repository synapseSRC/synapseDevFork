package com.synapse.social.studioasinc.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.synapse.social.studioasinc.data.repository.SettingsRepositoryImpl
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Navigation host for the Settings feature.
 *
 * Manages navigation between all settings screens with consistent transitions
 * and state preservation. Uses Jetpack Compose Navigation with Material 3
 * motion design.
 *
 * Requirements: 1.2, 1.3
 *
 * @param modifier Modifier to be applied to the NavHost
 * @param navController Navigation controller for managing navigation state
 * @param startDestination Initial destination route (defaults to Settings Hub)
 * @param onBackClick Callback to exit the settings flow (finish activity)
 * @param onNavigateToProfileEdit Callback to navigate to ProfileEditActivity
 * @param onNavigateToChatPrivacy Callback to navigate to ChatPrivacySettingsActivity
 * @param onLogout Callback to perform logout
 */
@Composable
fun SettingsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = SettingsDestination.ROUTE_HUB,
    onBackClick: () -> Unit = {},
    onNavigateToProfileEdit: () -> Unit = {},
    onNavigateToChatPrivacy: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsRepository = SettingsRepositoryImpl.getInstance(context)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { SettingsAnimations.enterTransition },
        exitTransition = { SettingsAnimations.exitTransition },
        popEnterTransition = { SettingsAnimations.popEnterTransition },
        popExitTransition = { SettingsAnimations.popExitTransition }
    ) {
        // Settings Hub - Main screen with categorized settings
        composable(route = SettingsDestination.ROUTE_HUB) {
            val viewModel: SettingsHubViewModel = hiltViewModel()
            SettingsHubScreen(
                viewModel = viewModel,
                onBackClick = onBackClick,
                onNavigateToCategory = { destination ->
                    navController.navigate(destination.route)
                }
            )
        }

        // Account Settings Screen
        composable(route = SettingsDestination.ROUTE_ACCOUNT) {
            val viewModel: AccountSettingsViewModel = viewModel()
            AccountSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditProfile = onNavigateToProfileEdit,
                onLogout = onLogout,
                onNavigateToRequestAccountInfo = {
                    navController.navigate(SettingsDestination.ROUTE_REQUEST_ACCOUNT_INFO)
                }
            )
        }

        // Request Account Information Screen
        composable(route = SettingsDestination.ROUTE_REQUEST_ACCOUNT_INFO) {
            val viewModel: RequestAccountInfoViewModel = viewModel()
            RequestAccountInfoScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Privacy & Security Settings Screen
        composable(route = SettingsDestination.ROUTE_PRIVACY) {
            val viewModel: PrivacySecurityViewModel = viewModel()
            PrivacySecurityScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBlockedUsers = {
                    // Placeholder - navigate to blocked users screen
                },
                onNavigateToMutedUsers = {
                    // Placeholder - navigate to muted users screen
                },
                onNavigateToActiveSessions = {
                    // Placeholder - navigate to active sessions screen
                }
            )
        }

        // Appearance Settings Screen
        composable(route = SettingsDestination.ROUTE_APPEARANCE) {
            val viewModel: AppearanceViewModel = viewModel()
            AppearanceScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChatCustomization = {
                    // Navigate to chat customization screen
                }
            )
        }

        // Notification Settings Screen
        composable(route = SettingsDestination.ROUTE_NOTIFICATIONS) {
            val viewModel: NotificationSettingsViewModel = hiltViewModel()
            NotificationSettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT) {
            ChatPlaceholderScreen(
                title = "Chat Settings",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT_THEME) {
            ChatPlaceholderScreen(
                title = "Chat Themes",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT_WALLPAPER) {
            ChatPlaceholderScreen(
                title = "Chat Wallpapers",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = SettingsDestination.ROUTE_CHAT_HISTORY_DELETION) {
            ChatPlaceholderScreen(
                title = "Chat History Deletion",
                onBackClick = { navController.popBackStack() }
            )
        }

        // Storage & Data Settings Screen
        composable(route = SettingsDestination.ROUTE_STORAGE) {
            val viewModel: StorageDataViewModel = viewModel(
                factory = StorageDataViewModelFactory(settingsRepository)
            )
            StorageDataScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController
            )
        }

        // Manage Storage Screen
        composable(route = SettingsDestination.ROUTE_MANAGE_STORAGE) {
            val viewModel: ManageStorageViewModel = hiltViewModel()
            ManageStorageScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Network Usage Screen
        composable(route = SettingsDestination.ROUTE_NETWORK_USAGE) {
            val viewModel: NetworkUsageViewModel = hiltViewModel()
            NetworkUsageScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Storage Provider Configuration Screen
        composable(route = SettingsDestination.ROUTE_STORAGE_PROVIDER) {
            // Reusing SettingsViewModel for now or creating a specific one if needed.
            // Since StorageProviderScreen uses SettingsViewModel, let's use that.
            // Note: SettingsViewModel handles AppSettingsManager directly.
            val viewModel: SettingsViewModel = viewModel()
            StorageProviderScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        // Language & Region Settings Screen
        composable(route = SettingsDestination.ROUTE_LANGUAGE) {
            val viewModel: LanguageRegionViewModel = viewModel()
            LanguageRegionScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // About & Support Settings Screen
        composable(route = SettingsDestination.ROUTE_ABOUT) {
            val viewModel: AboutSupportViewModel = viewModel()
            AboutSupportScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToLicenses = {
                    navController.navigate(SettingsDestination.ROUTE_LICENSES)
                },
                viewModel = viewModel
            )
        }

        // Open Source Licenses Screen
        composable(route = SettingsDestination.ROUTE_LICENSES) {
            LicensesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // API Key Settings Screen
        composable(route = SettingsDestination.ROUTE_API_KEY) {
            val viewModel: ApiKeySettingsViewModel = hiltViewModel()
            ApiKeySettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Synapse Plus Settings Screen
        composable(route = SettingsDestination.ROUTE_SYNAPSE_PLUS) {
            SynapsePlusScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Avatar Settings Screen
        composable(route = SettingsDestination.ROUTE_AVATAR) {
            AvatarScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Favourites Settings Screen
        composable(route = SettingsDestination.ROUTE_FAVOURITES) {
            FavouritesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Accessibility Settings Screen
        composable(route = SettingsDestination.ROUTE_ACCESSIBILITY) {
            AccessibilityScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Search Settings (Placeholder)
        composable(route = SettingsDestination.ROUTE_SEARCH) {
            // For now, just show a blank screen or go back
            // Ideally this would be SettingsSearchScreen()
            android.widget.Toast.makeText(LocalContext.current, "Search coming soon", android.widget.Toast.LENGTH_SHORT).show()
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }
    }
}

/**
 * Placeholder screen for chat-related features that are not yet implemented.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatPlaceholderScreen(
    title: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_revert),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chat feature not implemented",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
