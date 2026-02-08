package com.synapse.social.studioasinc.data.local.database

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)
}
