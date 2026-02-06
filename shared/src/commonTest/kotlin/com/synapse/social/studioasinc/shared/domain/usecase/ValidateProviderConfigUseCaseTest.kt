package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateProviderConfigUseCaseTest {

    private val useCase = ValidateProviderConfigUseCase()

    @Test
    fun testDefaultProviderIsAlwaysValid() {
        val config = StorageConfig()
        assertTrue(useCase(config, StorageProvider.DEFAULT))
    }

    @Test
    fun testImgBBValidation() {
        val validConfig = StorageConfig(imgBBKey = "123")
        val invalidConfig = StorageConfig(imgBBKey = "")

        assertTrue(useCase(validConfig, StorageProvider.IMGBB))
        assertFalse(useCase(invalidConfig, StorageProvider.IMGBB))
    }

    @Test
    fun testCloudinaryValidation() {
        val validConfig = StorageConfig(
            cloudinaryCloudName = "cloud",
            cloudinaryApiKey = "key",
            cloudinaryApiSecret = "secret"
        )
        val invalidConfig = StorageConfig(
            cloudinaryCloudName = "cloud",
            cloudinaryApiKey = "",
            cloudinaryApiSecret = "secret"
        )

        assertTrue(useCase(validConfig, StorageProvider.CLOUDINARY))
        assertFalse(useCase(invalidConfig, StorageProvider.CLOUDINARY))
    }

    @Test
    fun testSupabaseValidation() {
        val validConfig = StorageConfig(
            supabaseUrl = "url",
            supabaseKey = "key",
            supabaseBucket = "bucket"
        )
        val invalidConfig = StorageConfig(
            supabaseUrl = "url",
            supabaseKey = "",
            supabaseBucket = "bucket"
        )

        assertTrue(useCase(validConfig, StorageProvider.SUPABASE))
        assertFalse(useCase(invalidConfig, StorageProvider.SUPABASE))
    }
}
