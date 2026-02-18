package com.synapse.social.studioasinc.data.local.database

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import com.synapse.social.studioasinc.shared.data.database.Post as SharedPost

interface PostDao {
    suspend fun insertAll(posts: List<SharedPost>)
    suspend fun deletePost(postId: String)
    suspend fun getPostById(postId: String): SharedPost?
    fun getAllPosts(): Flow<List<SharedPost>>
    suspend fun getAllPostIds(): List<String>
    suspend fun deletePosts(ids: List<String>)
    fun getVideoPostsPaged(): PagingSource<Int, SharedPost>
}
