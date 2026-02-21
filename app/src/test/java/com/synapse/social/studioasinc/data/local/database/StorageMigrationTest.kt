package com.synapse.social.studioasinc.data.local.database

import android.content.Context
import android.content.SharedPreferences
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class StorageMigrationTest {

    private lateinit var context: Context
    private lateinit var storageRepository: StorageRepository
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mock()
        storageRepository = mock()
        prefs = mock()
        editor = mock()

        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putInt(any(), any())).thenReturn(editor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `migrateIfNeeded migrates from 0 to 2 and configures ImgBB`() = runTest {
        // Given
        val imgBBApiKey = "test_api_key"
        val storageMigration = StorageMigration(context, storageRepository, imgBBApiKey)

        whenever(prefs.getInt("storage_migration_version", 0)).thenReturn(0)

        val defaultConfig = StorageConfig()
        whenever(storageRepository.getStorageConfig()).thenReturn(flowOf(defaultConfig))

        // When
        storageMigration.migrateIfNeeded()

        // Then
        // Verify version 0 migration
        verify(storageRepository).ensureDefault()

        // Verify version 1 migration
        verify(storageRepository).updateImgBBConfig(imgBBApiKey)
        verify(storageRepository).updatePhotoProvider(StorageProvider.IMGBB)

        // Verify version update calls
        verify(editor).putInt("storage_migration_version", 1)
        verify(editor).putInt("storage_migration_version", 2)
        verify(editor, times(2)).commit()
    }

    @Test
    fun `migrateIfNeeded does not overwrite existing photo provider`() = runTest {
        // Given
        val imgBBApiKey = "test_api_key"
        val storageMigration = StorageMigration(context, storageRepository, imgBBApiKey)

        whenever(prefs.getInt("storage_migration_version", 0)).thenReturn(1) // Already at version 1

        val existingConfig = StorageConfig(
            photoProvider = StorageProvider.SUPABASE
        )
        whenever(storageRepository.getStorageConfig()).thenReturn(flowOf(existingConfig))

        // When
        storageMigration.migrateIfNeeded()

        // Then
        // Verify version 1 migration does NOT change provider
        verify(storageRepository, never()).updatePhotoProvider(any())
        verify(storageRepository, never()).updateImgBBConfig(any())

        // Verify version update
        verify(editor).putInt("storage_migration_version", 2)
        verify(editor).commit()
    }

    @Test
    fun `migrateIfNeeded does nothing if api key is missing`() = runTest {
        // Given
        val imgBBApiKey = ""
        val storageMigration = StorageMigration(context, storageRepository, imgBBApiKey)

        whenever(prefs.getInt("storage_migration_version", 0)).thenReturn(1)

        // When
        storageMigration.migrateIfNeeded()

        // Then
        verify(storageRepository, never()).updatePhotoProvider(any())
        verify(storageRepository, never()).updateImgBBConfig(any())

        // Verify version update
        verify(editor).putInt("storage_migration_version", 2)
        verify(editor).commit()
    }
}
