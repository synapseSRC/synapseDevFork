package com.synapse.social.studioasinc.shared.di

import com.synapse.social.studioasinc.shared.core.media.MediaCache
import com.synapse.social.studioasinc.shared.data.repository.MediaInteractionRepository
import com.synapse.social.studioasinc.shared.domain.usecase.feed.*
import com.synapse.social.studioasinc.shared.domain.usecase.profile.*
import com.synapse.social.studioasinc.shared.domain.usecase.search.*
import com.synapse.social.studioasinc.shared.domain.usecase.user.*
import org.koin.dsl.module

val sharedModule = module {
    // Core utilities
    single { MediaCache() }
    
    // Repositories
    single { MediaInteractionRepository() }
    
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
