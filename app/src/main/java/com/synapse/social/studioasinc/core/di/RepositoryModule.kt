package com.synapse.social.studioasinc.core.di

import android.content.Context
import android.content.SharedPreferences
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.repository.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import com.synapse.social.studioasinc.shared.domain.usecase.*
import com.synapse.social.studioasinc.shared.domain.usecase.notification.*
import com.synapse.social.studioasinc.shared.data.local.SecureStorage
import com.synapse.social.studioasinc.shared.data.local.AndroidSecureStorage
import com.synapse.social.studioasinc.shared.data.local.database.CommentDao
import com.synapse.social.studioasinc.shared.data.source.remote.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Singleton
import io.github.jan.supabase.SupabaseClient as SupabaseClientType

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
    ): AuthRepository {
        return AuthRepository(client)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(client: SupabaseClientType): SettingsRepository {
        return SettingsRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideUserRepository(storageDatabase: StorageDatabase, client: SupabaseClientType): UserRepository {
        return UserRepositoryImpl(storageDatabase, client)
    }

    @Provides
    @Singleton
    fun providePostRepository(
        storageDatabase: StorageDatabase,
        client: SupabaseClientType
    ): PostRepository {
        return PostRepositoryImpl(storageDatabase, client)
    }

    @Provides
    @Singleton
    fun provideUsernameRepository(client: SupabaseClientType): UsernameRepository {
        return UsernameRepositoryImpl(client)
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
    fun providePostInteractionRepository(client: SupabaseClientType): PostInteractionRepository {
        return PostInteractionRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideProfileActionRepository(client: SupabaseClientType): ProfileActionRepository {
        return ProfileActionRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideReactionRepository(client: SupabaseClientType): ReactionRepository {
        return ReactionRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun providePostDetailRepository(
        client: SupabaseClientType,
        reactionRepository: ReactionRepository
    ): PostDetailRepository {
        return PostDetailRepositoryImpl(client, reactionRepository)
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
        return CommentRepositoryImpl(
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
        return PollRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(client: SupabaseClientType): BookmarkRepository {
        return BookmarkRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideReshareRepository(client: SupabaseClientType): ReshareRepository {
        return ReshareRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideReportRepository(client: SupabaseClientType): ReportRepository {
        return ReportRepositoryImpl(client)
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
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
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
        imgBBUploadService: ImgBBUploadService,
        cloudinaryUploadService: CloudinaryUploadService,
        supabaseUploadService: SupabaseUploadService,
        r2UploadService: R2UploadService
    ): UploadMediaUseCase {
        return UploadMediaUseCase(
            repository,
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
        authRepository: AuthRepository
    ): GetNotificationsUseCase {
        return GetNotificationsUseCase(notificationRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideMarkNotificationAsReadUseCase(
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository
    ): MarkNotificationAsReadUseCase {
        return MarkNotificationAsReadUseCase(notificationRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideSubscribeToNotificationsUseCase(
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository
    ): SubscribeToNotificationsUseCase {
        return SubscribeToNotificationsUseCase(notificationRepository, authRepository)
    }
}
