package com.synapse.social.studioasinc.shared.domain.usecase.post

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RevokeVoteUseCase(private val repository: PollRepository) {
    operator fun invoke(postId: String): Flow<Result<Unit>> = flow {
        emit(repository.revokeVote(postId))
    }
}
