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
import com.synapse.social.studioasinc.shared.data.datasource.IAuthDataSource
import com.synapse.social.studioasinc.shared.data.datasource.AuthDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideAuthDataSource(): IAuthDataSource {
        // Implementation would inject proper auth service
        return AuthDataSource(authService = TODO("Inject auth service"))
    }

    // Use Case Provides
    @Provides
    @Singleton
    fun provideLikePostUseCase(): LikePostUseCase {
        return LikePostUseCase(TODO("Inject repository"))
    }

    @Provides
    @Singleton
    fun provideGetProfileUseCase(): GetProfileUseCase {
        return GetProfileUseCase(TODO("Inject repository"))
    }

    // Add more provides as needed...
}
