package com.synapse.social.studioasinc.data.local.database

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("SELECT * FROM posts")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT id FROM posts")
    suspend fun getAllPostIds(): List<String>

    @Query("DELETE FROM posts WHERE id IN (:ids)")
    suspend fun deletePosts(ids: List<String>)

    @Query("SELECT * FROM posts")
    fun getVideoPostsPaged(): PagingSource<Int, PostEntity>
}
