package com.synapse.social.studioasinc.feature.profile.editprofile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.synapse.social.studioasinc.R
import com.synapse.social.studioasinc.feature.profile.editprofile.components.sections.SectionCard
import com.synapse.social.studioasinc.presentation.editprofile.components.GenderSelector
import com.synapse.social.studioasinc.presentation.editprofile.components.ProfileFormFields
import com.synapse.social.studioasinc.presentation.editprofile.components.ProfileImageSection
import com.synapse.social.studioasinc.ui.settings.SettingsCard
import com.synapse.social.studioasinc.ui.settings.SettingsNavigationItem
import com.synapse.social.studioasinc.ui.settings.SettingsSpacing
import com.synapse.social.studioasinc.feature.shared.theme.SynapseTheme
import com.synapse.social.studioasinc.shared.domain.model.PrivacyLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRegionSelection: (String) -> Unit,
    onNavigateToPhotoHistory: (String) -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(EditProfileEvent.AvatarSelected(uri))
        }
    }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(EditProfileEvent.CoverSelected(uri))
        }
    }

    fun launchAvatarPicker() {
        try {
            avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "No photo picker app found. Please install a file manager.", Toast.LENGTH_LONG).show()
        }
    }

    fun launchCoverPicker() {
        try {
            coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "No photo picker app found. Please install a file manager.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                EditProfileNavigation.NavigateBack -> onNavigateBack()
                EditProfileNavigation.NavigateToRegionSelection -> {
                    onNavigateToRegionSelection(viewModel.uiState.value.selectedRegion ?: "")
                }
                EditProfileNavigation.NavigateToProfileHistory -> {
                    onNavigateToPhotoHistory("PROFILE")
                }
                EditProfileNavigation.NavigateToCoverHistory -> {
                    onNavigateToPhotoHistory("COVER")
                }
                EditProfileNavigation.NavigateToPrivacy -> {
                    onNavigateToPrivacy()
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    SynapseTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MediumTopAppBar(
                    title = { Text("Edit Profile") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onEvent(EditProfileEvent.BackClicked) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToPrivacy) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Privacy")
                        }
                        if (uiState.isSaving) {
                             CircularProgressIndicator(
                                 modifier = Modifier.padding(end = 16.dp).size(24.dp)
                             )
                        } else {
                            TextButton(
                                onClick = { viewModel.onEvent(EditProfileEvent.SaveClicked) },
                                enabled = uiState.hasChanges &&
                                          uiState.usernameValidation !is UsernameValidation.Error &&
                                          uiState.nicknameError == null &&
                                          uiState.bioError == null,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Save")
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = SettingsSpacing.screenPadding,
                        end = SettingsSpacing.screenPadding,
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = paddingValues.calculateBottomPadding() + 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(SettingsSpacing.sectionSpacing)
                ) {

                    item {
                        ProfileImageSection(
                            coverUrl = uiState.coverUrl,
                            avatarUrl = uiState.avatarUrl,
                            avatarUploadState = uiState.avatarUploadState,
                            coverUploadState = uiState.coverUploadState,
                            onCoverClick = { launchCoverPicker() },
                            onAvatarClick = { launchAvatarPicker() },
                            onRetryAvatarUpload = {
                                viewModel.onEvent(EditProfileEvent.RetryAvatarUpload)
                            },
                            onRetryCoverUpload = {
                                viewModel.onEvent(EditProfileEvent.RetryCoverUpload)
                            }
                        )
                    }

                    item {
                        ProfileFormFields(
                            username = uiState.username,
                            onUsernameChange = { viewModel.onEvent(EditProfileEvent.UsernameChanged(it)) },
                            usernameValidation = uiState.usernameValidation,
                            nickname = uiState.nickname,
                            onNicknameChange = { viewModel.onEvent(EditProfileEvent.NicknameChanged(it)) },
                            nicknameError = uiState.nicknameError,
                            bio = uiState.bio,
                            onBiographyChange = { viewModel.onEvent(EditProfileEvent.BiographyChanged(it)) },
                            bioError = uiState.bioError
                        )
                    }

                    item {
                        GenderSelector(
                            selectedGender = uiState.selectedGender,
                            onGenderSelected = { viewModel.onEvent(EditProfileEvent.GenderSelected(it)) }
                        )
                    }

                    item {
                        SettingsCard {
                            SettingsNavigationItem(
                                title = "Region",
                                subtitle = uiState.selectedRegion ?: "Not set",
                                icon = R.drawable.ic_location,
                                onClick = {
                                    onNavigateToRegionSelection(uiState.selectedRegion ?: "")
                                }
                            )
                        }
                    }

                    // New Sections
                    item {
                        SectionCard(
                            title = "Social Links",
                            privacyLevel = PrivacyLevel.PUBLIC, // Fetch from state in real app
                            onEditClick = { /* Open Dialog */ },
                            onPrivacyClick = onNavigateToPrivacy
                        ) {
                            Text("Facebook, Instagram...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    item {
                        SectionCard(
                            title = "Work History",
                            privacyLevel = PrivacyLevel.FRIENDS,
                            onEditClick = { /* Open Dialog */ },
                            onPrivacyClick = onNavigateToPrivacy
                        ) {
                            Text("Add work experience", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // ... Add other sections similarly

                    item {
                        SettingsCard {
                            SettingsNavigationItem(
                                title = "Profile Photo History",
                                subtitle = "View and restore previous photos",
                                icon = null,
                                onClick = { viewModel.onEvent(EditProfileEvent.ProfileHistoryClicked) }
                            )

                            com.synapse.social.studioasinc.ui.settings.SettingsDivider()

                            SettingsNavigationItem(
                                title = "Cover Photo History",
                                subtitle = "View and restore previous covers",
                                icon = null,
                                onClick = { viewModel.onEvent(EditProfileEvent.CoverHistoryClicked) }
                            )
                        }
                    }
                }
            }
        }
    }
}
