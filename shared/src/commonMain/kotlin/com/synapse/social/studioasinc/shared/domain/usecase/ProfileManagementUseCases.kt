package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.domain.repository.ProfileSectionsRepository

class UploadProfileImageUseCase(
    private val repository: ProfileSectionsRepository
) {
    suspend fun uploadAvatar(userId: String, filePath: String): Result<String> =
        repository.uploadAvatar(userId, filePath)

    suspend fun uploadCover(userId: String, filePath: String): Result<String> =
        repository.uploadCover(userId, filePath)
}

class CheckUsernameAvailabilityUseCase(
    private val repository: ProfileSectionsRepository
) {
    suspend operator fun invoke(username: String, currentUserId: String): Result<Boolean> =
        repository.checkUsernameAvailability(username, currentUserId)
}

class SyncUsernameChangeUseCase(
    private val repository: ProfileSectionsRepository
) {
    suspend operator fun invoke(oldUsername: String, newUsername: String, userId: String): Result<Unit> =
        repository.syncUsernameChange(oldUsername, newUsername, userId)
}
