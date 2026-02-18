// TODO: Migrate to shared module (See MIGRATION_PLAN.md)
package com.synapse.social.studioasinc.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserEntity::class, PostEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    // CommentDao removed as part of migration to shared module

    companion object {
        fun getDatabase(context: Context): AppDatabase {
             throw NotImplementedError("Stub")
        }
    }
}
