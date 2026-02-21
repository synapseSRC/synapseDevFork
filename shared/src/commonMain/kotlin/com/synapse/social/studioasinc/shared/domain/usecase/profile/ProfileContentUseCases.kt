package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.UserProfile
import com.synapse.social.studioasinc.shared.domain.model.MediaItem
import com.synapse.social.studioasinc.shared.domain.model.Reel

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
    suspend operator fun invoke(userId: String): Result<List<MediaItem>> {
        return profileRepository.getProfilePhotos(userId)
    }
}

class GetProfileReelsUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Reel>> {
        return profileRepository.getProfileReels(userId)
    }
}

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, profile: UserProfile): Result<UserProfile> {
        return profileRepository.updateProfile(userId, profile)
    }
}
