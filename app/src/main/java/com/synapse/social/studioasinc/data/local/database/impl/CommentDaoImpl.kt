package com.synapse.social.studioasinc.data.local.database.impl

import com.synapse.social.studioasinc.data.local.database.CommentDao
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.database.Comment as SharedComment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers

class CommentDaoImpl @Inject constructor(
    private val db: StorageDatabase
) : CommentDao {
    override fun getCommentsForPost(postId: String): Flow<List<SharedComment>> {
        return db.commentQueries.selectByPostId(postId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun insertAll(comments: List<SharedComment>) {
        db.transaction {
            comments.forEach { comment ->
                db.commentQueries.insertComment(comment)
            }
        }
    }
}
