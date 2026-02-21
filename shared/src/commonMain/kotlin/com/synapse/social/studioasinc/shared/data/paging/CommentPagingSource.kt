package com.synapse.social.studioasinc.shared.data.paging

import com.synapse.social.studioasinc.shared.data.repository.CommentRepository
import com.synapse.social.studioasinc.shared.domain.model.CommentWithUser

class CommentPagingSource(
    private val repository: CommentRepository,
    private val postId: String
) {
    suspend fun loadPage(page: Int, pageSize: Int): Result<List<CommentWithUser>> = runCatching {
        val offset = page * pageSize
        val response = repository.fetchComments(postId, limit = pageSize, offset = offset)
        response.getOrThrow()
    }
}
