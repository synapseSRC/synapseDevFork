package com.synapse.social.studioasinc.shared.domain.usecase.post

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReportPostUseCase(private val repository: ReportRepository) {
    operator fun invoke(postId: String, reporterId: String, reason: String): Flow<Result<Unit>> = flow {
        emit(repository.createReport(postId, reporterId, reason))
    }
}
