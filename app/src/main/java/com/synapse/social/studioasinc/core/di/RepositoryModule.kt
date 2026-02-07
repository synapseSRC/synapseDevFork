package com.synapse.social.studioasinc.core.di

import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.data.local.auth.TokenManager
import com.synapse.social.studioasinc.shared.data.auth.TokenManager as SharedTokenManager
import com.synapse.social.studioasinc.data.repository.*
import com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository as SharedAuthRepository
import com.synapse.social.studioasinc.shared.data.repository.ReelRepository
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.data.local.database.*
import com.synapse.social.studioasinc.data.local.AppSettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient as SupabaseClientType
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSettings(@ApplicationContext context: Context): Settings {
        val sharedPrefs = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)
        return SharedPreferencesSettings(sharedPrefs)
    }

    @Provides
    @Singleton
    fun provideSharedTokenManager(settings: Settings): SharedTokenManager {
        return SharedTokenManager(settings)
    }

    @Provides
    @Singleton
    fun provideSharedAuthRepository(): SharedAuthRepository {
        return SharedAuthRepository()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        tokenManager: TokenManager,
        sharedAuthRepository: SharedAuthRepository
    ): AuthRepository {
        return AuthRepository(tokenManager, sharedAuthRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, client: SupabaseClientType): UserRepository {
        return UserRepository(userDao, client)
    }

    @Provides
    @Singleton
    fun providePostRepository(
        postDao: PostDao,
        client: SupabaseClientType
    ): PostRepository {
        return PostRepository(postDao, client)
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
        client: SupabaseClientType,
        commentDao: CommentDao,
        reactionRepository: ReactionRepository
    ): CommentRepository {
        return CommentRepository(commentDao, client, reactionRepository)
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
    fun provideNotificationRepository(client: SupabaseClientType): NotificationRepository {
        return NotificationRepository(client)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        db: com.synapse.social.studioasinc.shared.data.database.StorageDatabase
    ): com.synapse.social.studioasinc.shared.domain.repository.StorageRepository {
        return com.synapse.social.studioasinc.shared.data.repository.StorageRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideKtorHttpClient(): io.ktor.client.HttpClient {
        return io.ktor.client.HttpClient()
    }

    @Provides
    @Singleton
    fun provideGetStorageConfigUseCase(
        repository: com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
    ): com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase {
        return com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateStorageProviderUseCase(
        repository: com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
    ): com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase {
        return com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUploadMediaUseCase(
        repository: com.synapse.social.studioasinc.shared.domain.repository.StorageRepository,
        httpClient: io.ktor.client.HttpClient,
        supabaseClient: SupabaseClientType
    ): com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase {
        return com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase(
            repository,
            com.synapse.social.studioasinc.shared.data.FileUploader(),
            com.synapse.social.studioasinc.shared.data.source.remote.ImgBBUploadService(httpClient),
            com.synapse.social.studioasinc.shared.data.source.remote.CloudinaryUploadService(httpClient),
            com.synapse.social.studioasinc.shared.data.source.remote.SupabaseUploadService(supabaseClient),
            com.synapse.social.studioasinc.shared.data.source.remote.R2UploadService(httpClient)
        )
    }
}
