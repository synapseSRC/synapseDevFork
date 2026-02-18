package com.synapse.social.studioasinc.data.local.database.impl

import com.synapse.social.studioasinc.data.local.database.PostDao
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.database.Post as SharedPost
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers

class PostDaoImpl @Inject constructor(
    private val db: StorageDatabase
) : PostDao {
    override suspend fun insertAll(posts: List<SharedPost>) {
        db.transaction {
            posts.forEach { post ->
                db.postQueries.insertPost(
                    id = post.id,
                    key = post.key,
                    authorUid = post.authorUid,
                    postText = post.postText,
                    postImage = post.postImage,
                    postType = post.postType,
                    postHideViewsCount = post.postHideViewsCount,
                    postHideLikeCount = post.postHideLikeCount,
                    postHideCommentsCount = post.postHideCommentsCount,
                    postDisableComments = post.postDisableComments,
                    postVisibility = post.postVisibility,
                    publishDate = post.publishDate,
                    timestamp = post.timestamp,
                    likesCount = post.likesCount,
                    commentsCount = post.commentsCount,
                    viewsCount = post.viewsCount,
                    resharesCount = post.resharesCount,
                    mediaItems = post.mediaItems,
                    isEncrypted = post.isEncrypted,
                    nonce = post.nonce,
                    encryptionKeyId = post.encryptionKeyId,
                    isDeleted = post.isDeleted,
                    isEdited = post.isEdited,
                    editedAt = post.editedAt,
                    deletedAt = post.deletedAt,
                    hasPoll = post.hasPoll,
                    pollQuestion = post.pollQuestion,
                    pollOptions = post.pollOptions,
                    pollEndTime = post.pollEndTime,
                    pollAllowMultiple = post.pollAllowMultiple,
                    hasLocation = post.hasLocation,
                    locationName = post.locationName,
                    locationAddress = post.locationAddress,
                    locationLatitude = post.locationLatitude,
                    locationLongitude = post.locationLongitude,
                    locationPlaceId = post.locationPlaceId,
                    youtubeUrl = post.youtubeUrl,
                    reactions = post.reactions,
                    userReaction = post.userReaction,
                    username = post.username,
                    avatarUrl = post.avatarUrl,
                    isVerified = post.isVerified,
                    metadata = post.metadata,
                    userPollVote = post.userPollVote
                )
            }
        }
    }

    override suspend fun deletePost(postId: String) {
        db.postQueries.deleteById(postId)
    }

    override suspend fun getPostById(postId: String): SharedPost? {
        return db.postQueries.selectById(postId).executeAsOneOrNull()
    }

    override fun getAllPosts(): Flow<List<SharedPost>> {
        return db.postQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun getAllPostIds(): List<String> {
        return db.postQueries.selectAll().executeAsList().map { it.id }
    }

    override suspend fun deletePosts(ids: List<String>) {
        db.transaction {
            ids.forEach { id ->
                db.postQueries.deleteById(id)
            }
        }
    }

    override fun getVideoPostsPaged(): PagingSource<Int, SharedPost> {
        return PostPagingSource(db)
    }
}

class PostPagingSource(private val db: StorageDatabase) : PagingSource<Int, SharedPost>() {
    override fun getRefreshKey(state: PagingState<Int, SharedPost>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SharedPost> {
        val posts = db.postQueries.selectAll().executeAsList()
        return LoadResult.Page(
            data = posts,
            prevKey = null,
            nextKey = null
        )
    }
}
