package com.synapse.social.studioasinc.shared.domain.usecase.post

import com.synapse.social.studioasinc.shared.data.repository.PostInteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UnlikePostUseCase @Inject constructor(private val repository: PostInteractionRepository) {
    operator fun invoke(postId: String, userId: String): Flow<Result<Unit>> = flow {
        emit(repository.unlikePost(postId, userId))
    }
}
