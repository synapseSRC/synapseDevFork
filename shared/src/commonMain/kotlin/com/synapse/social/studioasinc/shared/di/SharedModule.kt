package com.synapse.social.studioasinc.shared.di

import com.synapse.social.studioasinc.shared.core.media.MediaCache
import com.synapse.social.studioasinc.shared.data.repository.MediaInteractionRepository
import com.synapse.social.studioasinc.shared.data.datasource.IAuthDataSource
import com.synapse.social.studioasinc.shared.data.datasource.AuthDataSource
import com.synapse.social.studioasinc.shared.data.datasource.ISupabaseDataSource
import com.synapse.social.studioasinc.shared.data.datasource.SupabaseDataSource
import com.synapse.social.studioasinc.shared.data.repository.AuthRepository
import com.synapse.social.studioasinc.shared.domain.usecase.feed.*
import com.synapse.social.studioasinc.shared.domain.usecase.profile.*
import com.synapse.social.studioasinc.shared.domain.usecase.search.*
import com.synapse.social.studioasinc.shared.domain.usecase.user.*
import org.koin.dsl.module

val sharedModule = module {
    // Core utilities
    single { MediaCache() }
    
    // DataSources
    single<IAuthDataSource> { AuthDataSource(get()) }
    single<ISupabaseDataSource> { SupabaseDataSource() }
    
    // Repositories
    single { MediaInteractionRepository() }
    single { AuthRepository(get<IAuthDataSource>()) }
    
    // Feed use cases
    factory { GetFeedPostsUseCase(get()) }
    factory { RefreshFeedUseCase(get()) }
    factory { LikePostUseCase(get()) }
    factory { BookmarkPostUseCase(get()) }
    
    // Profile use cases
    factory { GetProfilePostsUseCase(get()) }
    factory { GetProfilePhotosUseCase(get()) }
    factory { GetProfileReelsUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    
    // User use cases
    factory { GetUserProfileUseCase(get()) }
    factory { SearchUsersUseCase(get()) }
    factory { CheckUsernameAvailabilityUseCase(get()) }
    
    // Search use cases
    factory { SearchPostsUseCase(get()) }
    factory { SearchHashtagsUseCase(get()) }
    factory { GetTrendingHashtagsUseCase(get()) }
    factory { SearchNewsUseCase(get()) }
    factory { GetSuggestedAccountsUseCase(get()) }
}
