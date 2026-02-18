package com.synapse.social.studioasinc.core.di

import android.content.Context
import com.synapse.social.studioasinc.data.local.database.UserDao
import com.synapse.social.studioasinc.data.local.database.PostDao
import com.synapse.social.studioasinc.data.local.database.CommentDao
import com.synapse.social.studioasinc.data.local.database.impl.UserDaoImpl
import com.synapse.social.studioasinc.data.local.database.impl.PostDaoImpl
import com.synapse.social.studioasinc.data.local.database.impl.CommentDaoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.synapse.social.studioasinc.shared.data.database.Post
import com.synapse.social.studioasinc.shared.data.database.Comment
import com.synapse.social.studioasinc.shared.data.database.User
import com.synapse.social.studioasinc.shared.data.database.*

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStorageDatabase(@ApplicationContext context: Context): StorageDatabase {
        val driver = AndroidSqliteDriver(StorageDatabase.Schema, context, "storage.db")
        return StorageDatabase(
            driver = driver,
            PostAdapter = Post.Adapter(
                mediaItemsAdapter = mediaItemListAdapter,
                pollOptionsAdapter = pollOptionListAdapter,
                reactionsAdapter = reactionMapAdapter,
                userReactionAdapter = reactionTypeAdapter,
                metadataAdapter = postMetadataAdapter,
                likesCountAdapter = intAdapter,
                commentsCountAdapter = intAdapter,
                viewsCountAdapter = intAdapter,
                resharesCountAdapter = intAdapter,
                userPollVoteAdapter = intAdapter
            ),
            CommentAdapter = Comment.Adapter(
                likesCountAdapter = intAdapter,
                repliesCountAdapter = intAdapter
            ),
            UserAdapter = User.Adapter(
                followersCountAdapter = intAdapter,
                followingCountAdapter = intAdapter,
                postsCountAdapter = intAdapter
            )
        )
    }

    @Provides
    fun provideUserDao(db: StorageDatabase): UserDao {
        return UserDaoImpl(db)
    }

    @Provides
    fun providePostDao(db: StorageDatabase): PostDao {
        return PostDaoImpl(db)
    }

    @Provides
    fun provideCommentDao(db: StorageDatabase): CommentDao {
        return CommentDaoImpl(db)
    }
}
