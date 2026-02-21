package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.local.SecureStorage
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull

class StorageRepositoryImpl(
    db: StorageDatabase,
    private val secureStorage: SecureStorage
) : StorageRepository {
    private val queries = db.storageConfigQueries

    companion object {
        private const val KEY_IMGBB = "imgbb_key"
        private const val KEY_CLOUDINARY_API_KEY = "cloudinary_api_key"
        private const val KEY_CLOUDINARY_API_SECRET = "cloudinary_api_secret"
        private const val KEY_SUPABASE = "supabase_key"
        private const val KEY_R2_ACCESS_KEY_ID = "r2_access_key_id"
        private const val KEY_R2_SECRET_ACCESS_KEY = "r2_secret_access_key"
    }

    override fun getStorageConfig(): Flow<StorageConfig> {
        return queries.getConfig().asFlow()
            .onStart { ensureDefault() }
            .mapToOneOrNull(Dispatchers.IO)
            .map { row ->
                if (row == null) return@map StorageConfig()




                val imgBBKey = secureStorage.getString(KEY_IMGBB)?.takeIf { it.isNotBlank() } ?: row.imgbb_key

                val cloudinaryApiKey = secureStorage.getString(KEY_CLOUDINARY_API_KEY)?.takeIf { it.isNotBlank() } ?: row.cloudinary_api_key
                val cloudinaryApiSecret = secureStorage.getString(KEY_CLOUDINARY_API_SECRET)?.takeIf { it.isNotBlank() } ?: row.cloudinary_api_secret

                val supabaseKey = secureStorage.getString(KEY_SUPABASE)?.takeIf { it.isNotBlank() } ?: row.supabase_key

                val r2AccessKeyId = secureStorage.getString(KEY_R2_ACCESS_KEY_ID)?.takeIf { it.isNotBlank() } ?: row.r2_access_key_id
                val r2SecretAccessKey = secureStorage.getString(KEY_R2_SECRET_ACCESS_KEY)?.takeIf { it.isNotBlank() } ?: row.r2_secret_access_key

                StorageConfig(
                    photoProvider = row.photo_provider.toStorageProvider(),
                    videoProvider = row.video_provider.toStorageProvider(),
                    otherProvider = row.other_provider.toStorageProvider(),
                    imgBBKey = imgBBKey,
                    cloudinaryCloudName = row.cloudinary_cloud_name,
                    cloudinaryApiKey = cloudinaryApiKey,
                    cloudinaryApiSecret = cloudinaryApiSecret,
                    supabaseUrl = row.supabase_url,
                    supabaseKey = supabaseKey,
                    supabaseBucket = row.supabase_bucket,
                    r2AccountId = row.r2_account_id,
                    r2AccessKeyId = r2AccessKeyId,
                    r2SecretAccessKey = r2SecretAccessKey,
                    r2BucketName = row.r2_bucket_name
                )
            }
    }

    override suspend fun saveStorageConfig(config: StorageConfig) = withContext(Dispatchers.IO) {

        secureStorage.save(KEY_IMGBB, config.imgBBKey)
        secureStorage.save(KEY_CLOUDINARY_API_KEY, config.cloudinaryApiKey)
        secureStorage.save(KEY_CLOUDINARY_API_SECRET, config.cloudinaryApiSecret)
        secureStorage.save(KEY_SUPABASE, config.supabaseKey)
        secureStorage.save(KEY_R2_ACCESS_KEY_ID, config.r2AccessKeyId)
        secureStorage.save(KEY_R2_SECRET_ACCESS_KEY, config.r2SecretAccessKey)

        queries.transaction {
            queries.updatePhotoProvider(config.photoProvider.name)
            queries.updateVideoProvider(config.videoProvider.name)
            queries.updateOtherProvider(config.otherProvider.name)

            queries.updateImgBB("")
            queries.updateCloudinary(config.cloudinaryCloudName, "", "")
            queries.updateSupabase(config.supabaseUrl, "", config.supabaseBucket)
            queries.updateR2(config.r2AccountId, "", "", config.r2BucketName)
        }
    }

    override suspend fun updatePhotoProvider(provider: StorageProvider) = withContext(Dispatchers.IO) {
        queries.updatePhotoProvider(provider.name)
    }

    override suspend fun updateVideoProvider(provider: StorageProvider) = withContext(Dispatchers.IO) {
        queries.updateVideoProvider(provider.name)
    }

    override suspend fun updateOtherProvider(provider: StorageProvider) = withContext(Dispatchers.IO) {
        queries.updateOtherProvider(provider.name)
    }

    override suspend fun updateImgBBConfig(key: String) = withContext(Dispatchers.IO) {
        secureStorage.save(KEY_IMGBB, key)
        queries.updateImgBB("")
    }

    override suspend fun updateCloudinaryConfig(cloudName: String, apiKey: String, apiSecret: String) = withContext(Dispatchers.IO) {
        secureStorage.save(KEY_CLOUDINARY_API_KEY, apiKey)
        secureStorage.save(KEY_CLOUDINARY_API_SECRET, apiSecret)
        queries.updateCloudinary(cloudName, "", "")
    }

    override suspend fun updateSupabaseConfig(url: String, key: String, bucket: String) = withContext(Dispatchers.IO) {
        secureStorage.save(KEY_SUPABASE, key)
        queries.updateSupabase(url, "", bucket)
    }

    override suspend fun updateR2Config(
        accountId: String,
        accessKeyId: String,
        secretAccessKey: String,
        bucketName: String
    ) = withContext(Dispatchers.IO) {
        secureStorage.save(KEY_R2_ACCESS_KEY_ID, accessKeyId)
        secureStorage.save(KEY_R2_SECRET_ACCESS_KEY, secretAccessKey)
        queries.updateR2(accountId, "", "", bucketName)
    }

    override suspend fun ensureDefault() = withContext(Dispatchers.IO) {
        queries.insertDefault()
        migrateSecrets()
    }

    private fun migrateSecrets() {
        val row = queries.getConfig().executeAsOneOrNull() ?: return

        if (row.imgbb_key.isNotBlank()) {
            secureStorage.save(KEY_IMGBB, row.imgbb_key)
            queries.updateImgBB("")
        }

        if (row.cloudinary_api_key.isNotBlank() || row.cloudinary_api_secret.isNotBlank()) {
            if (row.cloudinary_api_key.isNotBlank()) secureStorage.save(KEY_CLOUDINARY_API_KEY, row.cloudinary_api_key)
            if (row.cloudinary_api_secret.isNotBlank()) secureStorage.save(KEY_CLOUDINARY_API_SECRET, row.cloudinary_api_secret)
            queries.updateCloudinary(row.cloudinary_cloud_name, "", "")
        }

        if (row.supabase_key.isNotBlank()) {
             secureStorage.save(KEY_SUPABASE, row.supabase_key)
             queries.updateSupabase(row.supabase_url, "", row.supabase_bucket)
        }

        if (row.r2_access_key_id.isNotBlank() || row.r2_secret_access_key.isNotBlank()) {
            if (row.r2_access_key_id.isNotBlank()) secureStorage.save(KEY_R2_ACCESS_KEY_ID, row.r2_access_key_id)
            if (row.r2_secret_access_key.isNotBlank()) secureStorage.save(KEY_R2_SECRET_ACCESS_KEY, row.r2_secret_access_key)
            queries.updateR2(row.r2_account_id, "", "", row.r2_bucket_name)
        }
    }

    private fun String.toStorageProvider(): StorageProvider {
        return try {
            StorageProvider.valueOf(this)
        } catch (e: Exception) {
            StorageProvider.DEFAULT
        }
    }

    override suspend fun uploadFile(
        fileBytes: ByteArray,
        fileName: String,
        provider: StorageProvider,
        bucketName: String?,
        onProgress: (Float) -> Unit
    ): Result<String> = runCatching {
        onProgress(0f)
        onProgress(1f)
        ""
    }
}

