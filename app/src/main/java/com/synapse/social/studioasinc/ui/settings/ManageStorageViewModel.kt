package com.synapse.social.studioasinc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageStorageViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _storageUsage = MutableStateFlow<StorageUsageBreakdown?>(null)
    val storageUsage: StateFlow<StorageUsageBreakdown?> = _storageUsage.asStateFlow()

    private val _largeFiles = MutableStateFlow<List<LargeFileInfo>>(emptyList())
    val largeFiles: StateFlow<List<LargeFileInfo>> = _largeFiles.asStateFlow()

    private val _chatStorageList = MutableStateFlow<List<ChatStorageInfo>>(emptyList())
    val chatStorageList: StateFlow<List<ChatStorageInfo>> = _chatStorageList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Simulate loading delay
            delay(1000)

            // Mock Data
            val total = 128L * 1024 * 1024 * 1024 // 128 GB
            val free = 45L * 1024 * 1024 * 1024 // 45 GB
            val synapse = 2L * 1024 * 1024 * 1024 + 500L * 1024 * 1024 // 2.5 GB
            val apps = total - free - synapse

            _storageUsage.value = StorageUsageBreakdown(
                totalSize = total,
                usedSize = total - free,
                freeSize = free,
                appsAndOtherSize = apps,
                synapseSize = synapse
            )

            _largeFiles.value = listOf(
                LargeFileInfo("1", "Video_20231024.mp4", 150L * 1024 * 1024, null, MediaType.VIDEO),
                LargeFileInfo("2", "Project_Presentation.pdf", 25L * 1024 * 1024, null, MediaType.DOCUMENT),
                LargeFileInfo("3", "Vacation_Full_HD.mp4", 450L * 1024 * 1024, null, MediaType.VIDEO)
            )

            _chatStorageList.value = listOf(
                ChatStorageInfo("c1", "Alice Wonderland", null, 500L * 1024 * 1024, System.currentTimeMillis()),
                ChatStorageInfo("c2", "Bob Builder", null, 120L * 1024 * 1024, System.currentTimeMillis() - 86400000),
                ChatStorageInfo("c3", "Family Group", null, 1500L * 1024 * 1024, System.currentTimeMillis() - 3600000),
                ChatStorageInfo("c4", "Work Updates", null, 50L * 1024 * 1024, System.currentTimeMillis() - 172800000)
            ).sortedByDescending { it.size }

            _isLoading.value = false
        }
    }
}
