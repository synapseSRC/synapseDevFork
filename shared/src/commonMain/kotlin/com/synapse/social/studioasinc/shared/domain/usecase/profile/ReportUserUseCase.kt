package com.synapse.social.studioasinc.shared.domain.usecase.profile

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReportUserUseCase(private val repository: ProfileRepository) {
    operator fun invoke(userId: String, reason: String): Flow<Result<Unit>> = flow {
        emit(repository.reportUser(userId, reason))
    }
}
