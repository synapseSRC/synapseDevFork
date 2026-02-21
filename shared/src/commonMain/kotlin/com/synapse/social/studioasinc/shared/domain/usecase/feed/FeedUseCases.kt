package com.synapse.social.studioasinc.shared.domain.usecase.feed

import com.synapse.social.studioasinc.shared.data.repository.PostInteractionRepository
import com.synapse.social.studioasinc.shared.data.repository.PostRepository
import com.synapse.social.studioasinc.shared.domain.model.Post

class GetFeedPostsUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int): Result<List<Post>> {
        return Result.success(emptyList()) // TODO: Implement feed
    }
}

class RefreshFeedUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(): Result<List<Post>> {
        return Result.success(emptyList()) // TODO: Implement feed
    }
}

class LikePostUseCase(
    private val postInteractionRepository: PostInteractionRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Result<Unit> {
        return postInteractionRepository.likePost(postId, userId)
    }
}

class BookmarkPostUseCase(
    private val postInteractionRepository: PostInteractionRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Result<Unit> {
        return postInteractionRepository.savePost(postId, userId)
    }
}
