package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.domain.model.MediaType
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UpdateStorageProviderUseCaseTest {

    @Test
    fun testUpdatePhotoProvider() = runTest {
        val repository = FakeStorageRepository()
        val useCase = UpdateStorageProviderUseCase(repository)

        useCase(MediaType.PHOTO, StorageProvider.IMGBB)

        assertEquals(StorageProvider.IMGBB, repository.updatedPhotoProvider)
        assertNull(repository.updatedVideoProvider)
        assertNull(repository.updatedOtherProvider)
    }

    @Test
    fun testUpdateVideoProvider() = runTest {
        val repository = FakeStorageRepository()
        val useCase = UpdateStorageProviderUseCase(repository)

        useCase(MediaType.VIDEO, StorageProvider.CLOUDINARY)

        assertEquals(StorageProvider.CLOUDINARY, repository.updatedVideoProvider)
        assertNull(repository.updatedPhotoProvider)
        assertNull(repository.updatedOtherProvider)
    }

    @Test
    fun testUpdateOtherProvider() = runTest {
        val repository = FakeStorageRepository()
        val useCase = UpdateStorageProviderUseCase(repository)

        useCase(MediaType.OTHER, StorageProvider.SUPABASE)

        assertEquals(StorageProvider.SUPABASE, repository.updatedOtherProvider)
        assertNull(repository.updatedPhotoProvider)
        assertNull(repository.updatedVideoProvider)
    }

    @Test
    fun testUpdateImageProviderIgnored() = runTest {
        val repository = FakeStorageRepository()
        val useCase = UpdateStorageProviderUseCase(repository)

        useCase(MediaType.IMAGE, StorageProvider.IMGBB)

        assertEquals(StorageProvider.IMGBB, repository.updatedPhotoProvider)
        assertNull(repository.updatedVideoProvider)
        assertNull(repository.updatedOtherProvider)
    }

    @Test
    fun testUpdateAudioProviderIgnored() = runTest {
        val repository = FakeStorageRepository()
        val useCase = UpdateStorageProviderUseCase(repository)

        useCase(MediaType.AUDIO, StorageProvider.IMGBB)

        assertNull(repository.updatedPhotoProvider)
        assertNull(repository.updatedVideoProvider)
        assertNull(repository.updatedOtherProvider)
    }

    private class FakeStorageRepository : StorageRepository {
        var updatedPhotoProvider: StorageProvider? = null
        var updatedVideoProvider: StorageProvider? = null
        var updatedOtherProvider: StorageProvider? = null

        override fun getStorageConfig(): Flow<StorageConfig> {
             return flowOf(StorageConfig())
        }

        override suspend fun saveStorageConfig(config: StorageConfig) {
            throw NotImplementedError("Not used in this test")
        }

        override suspend fun updatePhotoProvider(provider: StorageProvider) {
            updatedPhotoProvider = provider
        }

        override suspend fun updateVideoProvider(provider: StorageProvider) {
            updatedVideoProvider = provider
        }

        override suspend fun updateOtherProvider(provider: StorageProvider) {
            updatedOtherProvider = provider
        }

        override suspend fun updateImgBBConfig(key: String) {
            throw NotImplementedError("Not used in this test")
        }

        override suspend fun updateCloudinaryConfig(cloudName: String, apiKey: String, apiSecret: String) {
            throw NotImplementedError("Not used in this test")
        }

        override suspend fun updateSupabaseConfig(url: String, key: String, bucket: String) {
            throw NotImplementedError("Not used in this test")
        }

        override suspend fun updateR2Config(accountId: String, accessKeyId: String, secretAccessKey: String, bucketName: String) {
            throw NotImplementedError("Not used in this test")
        }

        override suspend fun ensureDefault() {
            throw NotImplementedError("Not used in this test")
        }
    }
}
