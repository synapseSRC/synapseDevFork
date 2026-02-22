package com.synapse.social.studioasinc.shared.domain.usecase.post

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReactToPostUseCase(private val repository: PostInteractionRepository) {
    operator fun invoke(postId: String, userId: String, reactionType: ReactionType, oldReaction: ReactionType? = null): Flow<Result<Unit>> = flow {
        emit(repository.toggleReaction(postId, "post", reactionType, oldReaction))
    }
}
