package com.synapse.social.studioasinc.shared.domain.usecase.profile
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.ProfileActionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class MuteUserUseCase (
    private val repository: ProfileActionRepository
) {
    operator fun invoke(userId: String, mutedUserId: String): Flow<Result<Unit>> = flow {
        emit(repository.muteUser(userId, mutedUserId))
    }
}
