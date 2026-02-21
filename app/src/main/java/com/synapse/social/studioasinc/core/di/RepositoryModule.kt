package com.synapse.social.studioasinc.core.di

import android.content.Context
import android.content.SharedPreferences
import com.synapse.social.studioasinc.data.repository.*
import com.synapse.social.studioasinc.shared.data.local.database.CommentDao
import com.synapse.social.studioasinc.data.local.AppSettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient as SupabaseClientType
import javax.inject.Singleton
import io.ktor.client.HttpClient
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
import com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase
import com.synapse.social.studioasinc.shared.data.repository.StorageRepositoryImpl
import com.synapse.social.studioasinc.shared.data.repository.ReelRepository
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.shared.data.local.SecureStorage
import com.synapse.social.studioasinc.shared.data.local.AndroidSecureStorage
import com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.notification.GetNotificationsUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.notification.MarkNotificationAsReadUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.notification.SubscribeToNotificationsUseCase
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository as SharedAuthRepository
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    @Named("ApplicationScope")
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideSharedAuthRepository(
        client: SupabaseClientType
    ): SharedAuthRepository {
        return SharedAuthRepository(client)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(storageDatabase: StorageDatabase, client: SupabaseClientType): UserRepository {
        return UserRepository(storageDatabase, client)
    }

    @Provides
    @Singleton
    fun provideSharedUserRepository(userRepository: UserRepository): com.synapse.social.studioasinc.shared.domain.repository.UserRepository {
        return userRepository
    }

    @Provides
    @Singleton
    fun providePostRepository(
        storageDatabase: StorageDatabase,
        client: SupabaseClientType
    ): PostRepository {
        return PostRepository(storageDatabase, client)
    }

    @Provides
    @Singleton
    fun provideUsernameRepository(): UsernameRepository {
        return UsernameRepository()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("synapse_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(client: SupabaseClientType): ProfileRepository {
        return ProfileRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun providePostInteractionRepository(): PostInteractionRepository {
        return PostInteractionRepository()
    }

    @Provides
    @Singleton
    fun provideProfileActionRepository(): ProfileActionRepository {
        return ProfileActionRepository()
    }

    @Provides
    @Singleton
    fun provideReactionRepository(client: SupabaseClientType): ReactionRepository {
        return ReactionRepository(client)
    }

    @Provides
    @Singleton
    fun providePostDetailRepository(
        client: SupabaseClientType,
        reactionRepository: ReactionRepository
    ): PostDetailRepository {
        return PostDetailRepository(client, reactionRepository)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(
        storageDatabase: StorageDatabase,
        client: SupabaseClientType,
        commentDao: CommentDao,
        userRepository: UserRepository,
        reactionRepository: ReactionRepository,
        @Named("ApplicationScope") externalScope: CoroutineScope
    ): CommentRepository {
        return CommentRepository(
            storageDatabase = storageDatabase,
            client = client,
            commentDao = commentDao,
            userRepository = userRepository,
            externalScope = externalScope,
            reactionRepository = reactionRepository
        )
    }

    @Provides
    @Singleton
    fun providePollRepository(client: SupabaseClientType): PollRepository {
        return PollRepository(client)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(client: SupabaseClientType): BookmarkRepository {
        return BookmarkRepository(client)
    }

    @Provides
    @Singleton
    fun provideReshareRepository(client: SupabaseClientType): ReshareRepository {
        return ReshareRepository(client)
    }

    @Provides
    @Singleton
    fun provideReportRepository(client: SupabaseClientType): ReportRepository {
        return ReportRepository(client)
    }

    @Provides
    @Singleton
    fun provideStoryRepository(
        @ApplicationContext context: Context,
        uploadMediaUseCase: UploadMediaUseCase
    ): StoryRepository {
        return StoryRepositoryImpl(context, uploadMediaUseCase)
    }

    @Provides
    @Singleton
    fun provideAppSettingsManager(@ApplicationContext context: Context): AppSettingsManager {
        return AppSettingsManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideReelRepository(): ReelRepository {
        return ReelRepository()
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        client: SupabaseClientType,
        @Named("ApplicationScope") externalScope: CoroutineScope
    ): NotificationRepository {
        return NotificationRepository(client, externalScope)
    }

    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage {
        return AndroidSecureStorage(context)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        db: StorageDatabase,
        secureStorage: SecureStorage
    ): StorageRepository {
        return StorageRepositoryImpl(db, secureStorage)
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
    fun provideFileUploader(@ApplicationContext context: Context): FileUploader {
        return FileUploader(context)
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
    ): GetStorageConfigUseCase {
        return GetStorageConfigUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateStorageProviderUseCase(
        repository: StorageRepository
    ): UpdateStorageProviderUseCase {
        return UpdateStorageProviderUseCase(repository)
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
    ): UploadMediaUseCase {
        return UploadMediaUseCase(
            repository,
            fileUploader,
            imgBBUploadService,
            cloudinaryUploadService,
            supabaseUploadService,
            r2UploadService
        )
    }

    @Provides
    @Singleton
    fun provideGetNotificationsUseCase(
        notificationRepository: NotificationRepository,
        authRepository: SharedAuthRepository
    ): GetNotificationsUseCase {
        return GetNotificationsUseCase(notificationRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideMarkNotificationAsReadUseCase(
        notificationRepository: NotificationRepository,
        authRepository: SharedAuthRepository
    ): MarkNotificationAsReadUseCase {
        return MarkNotificationAsReadUseCase(notificationRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideSubscribeToNotificationsUseCase(
        notificationRepository: NotificationRepository,
        authRepository: SharedAuthRepository
    ): SubscribeToNotificationsUseCase {
        return SubscribeToNotificationsUseCase(notificationRepository, authRepository)
    }
}
