package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.domain.repository.ProfileActionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class ArchiveProfileUseCase constructor(
    private val repository: ProfileActionRepository
) {
    operator fun invoke(userId: String, isArchived: Boolean): Flow<Result<Unit>> = flow {
        emit(repository.archiveProfile(userId, isArchived))
    }
}
