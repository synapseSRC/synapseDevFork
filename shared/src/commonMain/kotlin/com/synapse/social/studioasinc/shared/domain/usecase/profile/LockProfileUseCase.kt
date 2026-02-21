package com.synapse.social.studioasinc.shared.domain.usecase.profile
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class LockProfileUseCase (
    private val repository: ProfileRepository
) {
    operator fun invoke(userId: String): Flow<Result<Unit>> = flow {
        emit(repository.lockProfile(userId))
    }
}
