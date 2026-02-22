package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.PostDetail
import kotlinx.coroutines.flow.Flow

interface PostDetailRepository {
    fun getPostDetail(postId: String): Flow<Result<PostDetail>>
}
