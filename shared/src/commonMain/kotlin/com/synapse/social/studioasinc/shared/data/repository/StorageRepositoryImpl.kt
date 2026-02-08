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

    override fun getStorageConfig(): Flow<StorageConfig> {
        return queries.getConfig().asFlow()
            .onStart { ensureDefault() }
            .mapToOneOrNull(Dispatchers.IO)
            .map { row ->
                if (row == null) return@map StorageConfig()

                // Retrieve secrets from SecureStorage, falling back to DB (if migration failed or for legacy support)
                // Note: migrateSecrets() in ensureDefault() should have moved them to SecureStorage and cleared DB.

                val imgBBKey = secureStorage.getString("imgbb_key")?.takeIf { it.isNotBlank() } ?: row.imgbb_key

                val cloudinaryApiKey = secureStorage.getString("cloudinary_api_key")?.takeIf { it.isNotBlank() } ?: row.cloudinary_api_key
                val cloudinaryApiSecret = secureStorage.getString("cloudinary_api_secret")?.takeIf { it.isNotBlank() } ?: row.cloudinary_api_secret

                val supabaseKey = secureStorage.getString("supabase_key")?.takeIf { it.isNotBlank() } ?: row.supabase_key

                val r2AccessKeyId = secureStorage.getString("r2_access_key_id")?.takeIf { it.isNotBlank() } ?: row.r2_access_key_id
                val r2SecretAccessKey = secureStorage.getString("r2_secret_access_key")?.takeIf { it.isNotBlank() } ?: row.r2_secret_access_key

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
        // Save secrets to SecureStorage
        secureStorage.save("imgbb_key", config.imgBBKey)
        secureStorage.save("cloudinary_api_key", config.cloudinaryApiKey)
        secureStorage.save("cloudinary_api_secret", config.cloudinaryApiSecret)
        secureStorage.save("supabase_key", config.supabaseKey)
        secureStorage.save("r2_access_key_id", config.r2AccessKeyId)
        secureStorage.save("r2_secret_access_key", config.r2SecretAccessKey)

        queries.transaction {
            queries.updatePhotoProvider(config.photoProvider.name)
            queries.updateVideoProvider(config.videoProvider.name)
            queries.updateOtherProvider(config.otherProvider.name)
            // Save non-secrets to DB, clear secrets
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
        secureStorage.save("imgbb_key", key)
        queries.updateImgBB("")
    }

    override suspend fun updateCloudinaryConfig(cloudName: String, apiKey: String, apiSecret: String) = withContext(Dispatchers.IO) {
        secureStorage.save("cloudinary_api_key", apiKey)
        secureStorage.save("cloudinary_api_secret", apiSecret)
        queries.updateCloudinary(cloudName, "", "")
    }

    override suspend fun updateSupabaseConfig(url: String, key: String, bucket: String) = withContext(Dispatchers.IO) {
        secureStorage.save("supabase_key", key)
        queries.updateSupabase(url, "", bucket)
    }

    override suspend fun updateR2Config(
        accountId: String,
        accessKeyId: String,
        secretAccessKey: String,
        bucketName: String
    ) = withContext(Dispatchers.IO) {
        secureStorage.save("r2_access_key_id", accessKeyId)
        secureStorage.save("r2_secret_access_key", secretAccessKey)
        queries.updateR2(accountId, "", "", bucketName)
    }

    override suspend fun ensureDefault() = withContext(Dispatchers.IO) {
        queries.insertDefault()
        migrateSecrets()
    }

    private fun migrateSecrets() {
        val row = queries.getConfig().executeAsOneOrNull() ?: return

        var migrated = false

        if (row.imgbb_key.isNotBlank()) {
            secureStorage.save("imgbb_key", row.imgbb_key)
            queries.updateImgBB("")
            migrated = true
        }

        if (row.cloudinary_api_key.isNotBlank() || row.cloudinary_api_secret.isNotBlank()) {
            if (row.cloudinary_api_key.isNotBlank()) secureStorage.save("cloudinary_api_key", row.cloudinary_api_key)
            if (row.cloudinary_api_secret.isNotBlank()) secureStorage.save("cloudinary_api_secret", row.cloudinary_api_secret)
            queries.updateCloudinary(row.cloudinary_cloud_name, "", "")
            migrated = true
        }

        if (row.supabase_key.isNotBlank()) {
             secureStorage.save("supabase_key", row.supabase_key)
             queries.updateSupabase(row.supabase_url, "", row.supabase_bucket)
             migrated = true
        }

        if (row.r2_access_key_id.isNotBlank() || row.r2_secret_access_key.isNotBlank()) {
            if (row.r2_access_key_id.isNotBlank()) secureStorage.save("r2_access_key_id", row.r2_access_key_id)
            if (row.r2_secret_access_key.isNotBlank()) secureStorage.save("r2_secret_access_key", row.r2_secret_access_key)
            queries.updateR2(row.r2_account_id, "", "", row.r2_bucket_name)
            migrated = true
        }
    }

    private fun String.toStorageProvider(): StorageProvider {
        return try {
            StorageProvider.valueOf(this)
        } catch (e: Exception) {
            StorageProvider.DEFAULT
        }
    }
}
