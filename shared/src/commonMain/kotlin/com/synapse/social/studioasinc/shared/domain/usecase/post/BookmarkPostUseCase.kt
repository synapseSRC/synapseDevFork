package com.synapse.social.studioasinc.shared.domain.usecase.post
import com.synapse.social.studioasinc.shared.domain.model.*

import com.synapse.social.studioasinc.shared.domain.repository.PostInteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class BookmarkPostUseCase (
    private val repository: PostInteractionRepository
) {
    suspend operator fun invoke(postId: String, userId: String, isBookmarked: Boolean): Flow<Result<Boolean>> = flow {
        val result = if (isBookmarked) {
            repository.unsavePost(postId, userId)
        } else {
            repository.savePost(postId, userId)
        }

        result.fold(
            onSuccess = { _: Unit -> emit(Result.success(!isBookmarked)) },
            onFailure = { e: Throwable -> emit(Result.failure(e)) }
        )
    }
}
