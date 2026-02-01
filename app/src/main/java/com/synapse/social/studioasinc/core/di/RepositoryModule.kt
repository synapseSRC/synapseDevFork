package com.synapse.social.studioasinc.core.di

import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.synapse.social.studioasinc.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.data.local.auth.TokenManager
import com.synapse.social.studioasinc.shared.data.auth.TokenManager as SharedTokenManager
import com.synapse.social.studioasinc.data.local.database.PostDao
import com.synapse.social.studioasinc.data.repository.PostRepository
import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository as SharedAuthRepository
import com.synapse.social.studioasinc.data.repository.SettingsRepository
import com.synapse.social.studioasinc.data.repository.SettingsRepositoryImpl
import com.synapse.social.studioasinc.data.repository.UserRepository
import com.synapse.social.studioasinc.data.repository.UsernameRepository
import com.synapse.social.studioasinc.data.repository.ProfileRepository
import com.synapse.social.studioasinc.data.repository.ProfileRepositoryImpl
import com.synapse.social.studioasinc.data.repository.PostInteractionRepository
import com.synapse.social.studioasinc.data.repository.ProfileActionRepository
import com.synapse.social.studioasinc.data.repository.StoryRepository
import com.synapse.social.studioasinc.data.repository.StoryRepositoryImpl
import com.synapse.social.studioasinc.data.repository.SearchRepository
import com.synapse.social.studioasinc.data.repository.SearchRepositoryImpl
import com.synapse.social.studioasinc.shared.data.repository.ReelRepository
import com.synapse.social.studioasinc.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.data.local.database.UserDao
import com.synapse.social.studioasinc.data.local.database.AppSettingsManager
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
    fun provideSharedAuthRepository(tokenManager: SharedTokenManager): SharedAuthRepository {
        return SharedAuthRepository(tokenManager)
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
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
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
    fun provideStoryRepository(
        @ApplicationContext context: Context,
        appSettingsManager: AppSettingsManager,
        imageCompressor: ImageCompressor
    ): StoryRepository {
        return StoryRepositoryImpl(context, appSettingsManager, imageCompressor)
    }

    @Provides
    @Singleton
    fun provideAppSettingsManager(@ApplicationContext context: Context): AppSettingsManager {
        return AppSettingsManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(): SearchRepository {
        return SearchRepositoryImpl()
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
}
