package com.synapse.social.studioasinc.core.di

import android.content.Context
import android.content.SharedPreferences
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.repository.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import com.synapse.social.studioasinc.shared.domain.usecase.post.*
import com.synapse.social.studioasinc.shared.domain.usecase.profile.*
import com.synapse.social.studioasinc.shared.domain.usecase.story.*
import com.synapse.social.studioasinc.shared.domain.usecase.*
import com.synapse.social.studioasinc.shared.domain.usecase.notification.*
import com.synapse.social.studioasinc.shared.data.local.SecureStorage
import com.synapse.social.studioasinc.shared.data.local.AndroidSecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
    fun providePostRepository(
        storageDatabase: StorageDatabase,
        client: SupabaseClient
    ): com.synapse.social.studioasinc.shared.domain.repository.PostRepository {
        return com.synapse.social.studioasinc.shared.data.repository.PostRepository(storageDatabase, client)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        storageDatabase: StorageDatabase,
        client: SupabaseClient
    ): com.synapse.social.studioasinc.shared.domain.repository.UserRepository {
        return com.synapse.social.studioasinc.shared.data.repository.UserRepository(storageDatabase, client)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(client: SupabaseClient): com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository {
        return ProfileRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun providePostInteractionRepository(client: SupabaseClient): com.synapse.social.studioasinc.shared.domain.repository.PostInteractionRepository {
        // We need a proper implementation for PostInteractionRepository
        // For now, let's use a dummy or create one if missing
        return object : com.synapse.social.studioasinc.shared.domain.repository.PostInteractionRepository {
            override suspend fun likePost(postId: String, userId: String) = Result.success(Unit)
            override suspend fun unlikePost(postId: String, userId: String) = Result.success(Unit)
            override suspend fun toggleReaction(postId: String, userId: String, reactionType: com.synapse.social.studioasinc.shared.domain.model.ReactionType, oldReaction: com.synapse.social.studioasinc.shared.domain.model.ReactionType?, skipCheck: Boolean) = Result.success(Unit)
        }
    }

    @Provides
    @Singleton
    fun provideCommentRepository(
        storageDatabase: StorageDatabase,
        client: SupabaseClient
    ): com.synapse.social.studioasinc.shared.domain.repository.CommentRepository {
        // We need to fix the CommentRepository implementation in shared
        return com.synapse.social.studioasinc.shared.data.repository.CommentRepository(storageDatabase, client)
    }

    // Use Case Provides
    @Provides
    @Singleton
    fun provideLikePostUseCase(repository: com.synapse.social.studioasinc.shared.domain.repository.PostInteractionRepository): LikePostUseCase {
        return LikePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetProfileUseCase(repository: com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository): GetProfileUseCase {
        return GetProfileUseCase(repository)
    }

    // Add more provides as needed...
}
