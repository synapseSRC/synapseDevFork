package com.synapse.social.studioasinc.shared.domain.usecase.post
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.PostInteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class UnsavePostUseCase (private val repository: PostInteractionRepository) {
    operator fun invoke(postId: String, userId: String): Flow<Result<Unit>> = flow {
        emit(repository.unsavePost(postId, userId))
    }
}
