package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.data.repository.ProfileActionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MuteUserUseCase @Inject constructor(
    private val repository: ProfileActionRepository
) {
    operator fun invoke(userId: String, mutedUserId: String): Flow<Result<Unit>> = flow {
        emit(repository.muteUser(userId, mutedUserId))
    }
}
