
package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.domain.model.StorageConfig
import com.synapse.social.studioasinc.shared.domain.model.StorageProvider
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne

class StorageRepositoryImpl(
    db: StorageDatabase
) : StorageRepository {
    private val queries = db.storageConfigQueries

    override fun getStorageConfig(): Flow<StorageConfig> {
        return queries.getConfig().asFlow().mapToOne(Dispatchers.IO).map { row ->
            StorageConfig(
                photoProvider = row.photo_provider.toStorageProvider(),
                videoProvider = row.video_provider.toStorageProvider(),
                otherProvider = row.other_provider.toStorageProvider(),
                imgBBKey = row.imgbb_key,
                cloudinaryCloudName = row.cloudinary_cloud_name,
                cloudinaryApiKey = row.cloudinary_api_key,
                cloudinaryApiSecret = row.cloudinary_api_secret,
                supabaseUrl = row.supabase_url,
                supabaseKey = row.supabase_key,
                supabaseBucket = row.supabase_bucket,
                r2AccountId = row.r2_account_id,
                r2AccessKeyId = row.r2_access_key_id,
                r2SecretAccessKey = row.r2_secret_access_key,
                r2BucketName = row.r2_bucket_name
            )
        }
    }

    override suspend fun saveStorageConfig(config: StorageConfig) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.updatePhotoProvider(config.photoProvider.name)
            queries.updateVideoProvider(config.videoProvider.name)
            queries.updateOtherProvider(config.otherProvider.name)
            queries.updateImgBB(config.imgBBKey)
            queries.updateCloudinary(config.cloudinaryCloudName, config.cloudinaryApiKey, config.cloudinaryApiSecret)
            queries.updateSupabase(config.supabaseUrl, config.supabaseKey, config.supabaseBucket)
            queries.updateR2(config.r2AccountId, config.r2AccessKeyId, config.r2SecretAccessKey, config.r2BucketName)
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
        queries.updateImgBB(key)
    }

    override suspend fun updateCloudinaryConfig(cloudName: String, apiKey: String, apiSecret: String) = withContext(Dispatchers.IO) {
        queries.updateCloudinary(cloudName, apiKey, apiSecret)
    }

    override suspend fun updateSupabaseConfig(url: String, key: String, bucket: String) = withContext(Dispatchers.IO) {
        queries.updateSupabase(url, key, bucket)
    }

    override suspend fun updateR2Config(
        accountId: String,
        accessKeyId: String,
        secretAccessKey: String,
        bucketName: String
    ) = withContext(Dispatchers.IO) {
        queries.updateR2(accountId, accessKeyId, secretAccessKey, bucketName)
    }

    suspend fun ensureDefault() = withContext(Dispatchers.IO) {
        queries.insertDefault()
    }

    private fun String.toStorageProvider(): StorageProvider {
        return try {
            StorageProvider.valueOf(this)
        } catch (e: Exception) {
            StorageProvider.DEFAULT
        }
    }
}
