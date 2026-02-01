package com.synapse.social.studioasinc.shared.data.local.database

// import androidx.room.Room
// import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

/*
actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<SharedDatabase> {
        val dbFilePath = NSHomeDirectory() + "/synapse_shared.db"
        return Room.databaseBuilder<SharedDatabase>(
            name = dbFilePath,
        )
    }
}
*/
class DatabaseFactory
