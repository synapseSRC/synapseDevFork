package com.synapse.social.studioasinc.core.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStorageDatabase(@ApplicationContext context: Context): StorageDatabase {
        val driver = AndroidSqliteDriver(StorageDatabase.Schema, context, "storage.db")
        return StorageDatabase(driver)
    }
}
