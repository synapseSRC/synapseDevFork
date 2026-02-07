import os

file_path = 'app/src/main/java/com/synapse/social/studioasinc/core/di/RepositoryModule.kt'

with open(file_path, 'r') as f:
    content = f.read()

# Add imports
imports = """import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.synapse.social.studioasinc.shared.data.FileUploader
import com.synapse.social.studioasinc.shared.data.source.remote.ImgBBUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.CloudinaryUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.SupabaseUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.R2UploadService
import com.synapse.social.studioasinc.shared.domain.usecase.ValidateProviderConfigUseCase
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
"""

# Find where to insert imports (after the last import)
last_import_idx = content.rfind('import ')
line_end_idx = content.find('\n', last_import_idx)
content = content[:line_end_idx+1] + imports + content[line_end_idx+1:]

# Identify the block to replace
start_marker = "    @Provides\n    @Singleton\n    fun provideStorageRepository("
start_idx = content.find(start_marker)

if start_idx == -1:
    print("Could not find start marker")
    exit(1)

# Keep the content before the marker
new_content = content[:start_idx]

# Append the new implementation
new_content += """    @Provides
    @Singleton
    fun provideStorageRepository(
        db: com.synapse.social.studioasinc.shared.data.database.StorageDatabase
    ): StorageRepository {
        return com.synapse.social.studioasinc.shared.data.repository.StorageRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideKtorHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideFileUploader(): FileUploader {
        return FileUploader()
    }

    @Provides
    @Singleton
    fun provideImgBBUploadService(httpClient: HttpClient): ImgBBUploadService {
        return ImgBBUploadService(httpClient)
    }

    @Provides
    @Singleton
    fun provideCloudinaryUploadService(httpClient: HttpClient): CloudinaryUploadService {
        return CloudinaryUploadService(httpClient)
    }

    @Provides
    @Singleton
    fun provideSupabaseUploadService(supabaseClient: SupabaseClientType): SupabaseUploadService {
        return SupabaseUploadService(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideR2UploadService(httpClient: HttpClient): R2UploadService {
        return R2UploadService(httpClient)
    }

    @Provides
    @Singleton
    fun provideValidateProviderConfigUseCase(): ValidateProviderConfigUseCase {
        return ValidateProviderConfigUseCase()
    }

    @Provides
    @Singleton
    fun provideGetStorageConfigUseCase(
        repository: StorageRepository
    ): com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase {
        return com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateStorageProviderUseCase(
        repository: StorageRepository
    ): com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase {
        return com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUploadMediaUseCase(
        repository: StorageRepository,
        fileUploader: FileUploader,
        imgBBUploadService: ImgBBUploadService,
        cloudinaryUploadService: CloudinaryUploadService,
        supabaseUploadService: SupabaseUploadService,
        r2UploadService: R2UploadService
    ): com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase {
        return com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase(
            repository,
            fileUploader,
            imgBBUploadService,
            cloudinaryUploadService,
            supabaseUploadService,
            r2UploadService
        )
    }
}
"""

with open(file_path, 'w') as f:
    f.write(new_content)

print("Successfully updated RepositoryModule.kt")
