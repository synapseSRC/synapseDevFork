package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.core.di

import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.core.media.processing.ImageCompressor
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.auth.TokenManager
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.shared.data.auth.TokenManager as SharedTokenManager
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.*
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.shared.data.repository.AuthRepository as SharedAuthRepository
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.shared.data.repository.ReelRepository
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.shared.data.repository.NotificationRepository
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.database.*
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
