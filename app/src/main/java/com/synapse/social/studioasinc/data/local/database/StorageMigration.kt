package com.synapse.social.studioasinc.data.local.database

import android.content.Context
import android.content.SharedPreferences
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class StorageMigration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageRepository: StorageRepository,
    @Named("ImgBBApiKey") private val imgBBApiKey: String
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun migrateIfNeeded() = withContext(Dispatchers.IO) {
        var currentVersion = prefs.getInt(MIGRATION_VERSION_KEY, 0)

        while (currentVersion < CURRENT_VERSION) {
            performMigration(currentVersion)
            currentVersion++
            prefs.edit().putInt(MIGRATION_VERSION_KEY, currentVersion).apply()
        }
    }

    private suspend fun performMigration(fromVersion: Int) {
        when (fromVersion) {
            0 -> {
                storageRepository.ensureDefault()
            }
            1 -> {
                if (imgBBApiKey.isNotBlank()) {
                    val config = storageRepository.getStorageConfig().first()
                    if (config.photoProvider == StorageProvider.DEFAULT) {
                        storageRepository.updateImgBBConfig(imgBBApiKey)
                        storageRepository.updatePhotoProvider(StorageProvider.IMGBB)
                    }
                }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "migration_prefs"
        private const val MIGRATION_VERSION_KEY = "storage_migration_version"
        private const val CURRENT_VERSION = 2
    }
}
