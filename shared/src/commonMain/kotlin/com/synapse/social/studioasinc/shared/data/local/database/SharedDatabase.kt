package com.synapse.social.studioasinc.shared.data.local.database

/*
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [UserEntity::class, IdentityKeyEntity::class], version = 1)
@ConstructedBy(SharedDatabaseConstructor::class)
abstract class SharedDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun identityKeyDao(): IdentityKeyDao
}

// The Room compiler generates the actual implementation
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object SharedDatabaseConstructor : RoomDatabaseConstructor<SharedDatabase>

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<SharedDatabase>
}
*/

// Placeholder to allow compilation until KSP environment issue is resolved
class SharedDatabasePlaceholder
