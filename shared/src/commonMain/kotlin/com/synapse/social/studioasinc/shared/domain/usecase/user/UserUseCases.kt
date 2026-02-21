package com.synapse.social.studioasinc.shared.domain.usecase.user

import com.synapse.social.studioasinc.shared.data.repository.UserRepository
import com.synapse.social.studioasinc.shared.domain.model.User

class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return userRepository.getUserById(userId).mapCatching { it ?: throw NoSuchElementException("User not found") }
    }
}

class SearchUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        return Result.success(emptyList())
    }
}

class CheckUsernameAvailabilityUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Result<Boolean> {
        return userRepository.isUsernameAvailable(username)
    }
}
