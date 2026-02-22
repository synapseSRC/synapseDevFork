package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(userId: String): Flow<Result<UserProfile?>> = flow {
        emit(repository.getProfile(userId))
    }
}
