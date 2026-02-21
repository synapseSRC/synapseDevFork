package com.synapse.social.studioasinc.shared.domain.usecase.profile
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.model.UserProfile
import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow


class GetProfileUseCase (private val repository: ProfileRepository) {
    suspend operator fun invoke(userId: String): Flow<Result<UserProfile>> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        return kotlinx.coroutines.flow.flow {
            val result = repository.getProfile(userId)
            emit(result.mapCatching { it ?: throw NoSuchElementException("Profile not found") })
        }
    }
}
