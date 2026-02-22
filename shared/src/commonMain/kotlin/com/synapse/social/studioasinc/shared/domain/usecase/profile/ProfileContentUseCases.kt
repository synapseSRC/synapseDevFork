package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProfilePostsUseCase(private val repository: ProfileRepository) {
    operator fun invoke(userId: String): Flow<Result<List<Post>>> = flow {
        emit(repository.getProfilePosts(userId))
    }
}

class GetProfilePhotosUseCase(private val repository: ProfileRepository) {
    operator fun invoke(userId: String): Flow<Result<List<MediaItem>>> = flow {
        emit(repository.getProfilePhotos(userId))
    }
}

class GetProfileReelsUseCase(private val repository: ProfileRepository) {
    operator fun invoke(userId: String): Flow<Result<List<Reel>>> = flow {
        emit(repository.getProfileReels(userId))
    }
}

class UpdateProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(userId: String, profile: UserProfile): Result<UserProfile> {
        return repository.updateProfile(userId, profile)
    }
}
