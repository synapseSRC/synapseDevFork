package com.synapse.social.studioasinc.shared.domain.usecase.post

import com.synapse.social.studioasinc.shared.domain.model.ReactionType
import com.synapse.social.studioasinc.shared.domain.repository.ReactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReactToPostUseCase constructor(private val repository: ReactionRepository) {
    operator fun invoke(
        postId: String,
        reactionType: ReactionType,
        oldReaction: ReactionType? = null
    ): Flow<Result<Unit>> = flow {
        emit(repository.toggleReaction(postId, "post", reactionType, oldReaction))
    }
}
