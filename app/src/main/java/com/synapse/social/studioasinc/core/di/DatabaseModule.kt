package com.synapse.social.studioasinc.core.di

import android.content.Context
import androidx.room.Room
import com.synapse.social.studioasinc.data.local.database.AppDatabase
import com.synapse.social.studioasinc.data.local.database.UserDao
import com.synapse.social.studioasinc.data.local.database.PostDao
import com.synapse.social.studioasinc.data.local.database.CommentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao {
        return db.userDao()
    }

    @Provides
    fun providePostDao(db: AppDatabase): PostDao {
        return db.postDao()
    }

    @Provides
    fun provideCommentDao(db: AppDatabase): CommentDao {
        return db.commentDao()
    }

    @Provides
    @Singleton
    fun provideStorageDatabase(): StorageDatabase {
        throw NotImplementedError("Stub for StorageDatabase")
    }
}
