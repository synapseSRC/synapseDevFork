package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.data.repository.ProfileRepository
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.User

class GetProfilePostsUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Post>> {
        return profileRepository.getProfilePosts(userId)
    }
}

class GetProfilePhotosUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Post>> {
        return profileRepository.getProfilePhotos(userId)
    }
}

class GetProfileReelsUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Post>> {
        return profileRepository.getProfileReels(userId)
    }
}

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return profileRepository.updateProfile(user)
    }
}
