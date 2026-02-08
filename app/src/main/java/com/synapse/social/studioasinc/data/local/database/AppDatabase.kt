package com.synapse.social.studioasinc.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, PostEntity::class, CommentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao

    companion object {
        fun getDatabase(context: Context): AppDatabase {
             throw NotImplementedError("Stub")
        }
    }
}
