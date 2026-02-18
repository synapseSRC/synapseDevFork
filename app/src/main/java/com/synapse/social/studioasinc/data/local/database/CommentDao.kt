package com.synapse.social.studioasinc.data.local.database

import kotlinx.coroutines.flow.Flow
import com.synapse.social.studioasinc.shared.data.database.Comment as SharedComment

interface CommentDao {
    fun getCommentsForPost(postId: String): Flow<List<SharedComment>>
    suspend fun insertAll(comments: List<SharedComment>)
}
