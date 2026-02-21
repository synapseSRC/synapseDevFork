package com.synapse.social.studioasinc.shared.domain.usecase.post
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class DeletePostUseCase (private val repository: PostRepository) {
    operator fun invoke(postId: String): Flow<Result<Unit>> = flow {
        emit(repository.deletePost(postId))
    }
}
