package com.synapse.social.studioasinc.shared.data.local.database

// import androidx.room.Dao
// import androidx.room.Insert
// import androidx.room.OnConflictStrategy
// import androidx.room.Query

// @Dao
interface UserDao {
    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): UserEntity?

    // @Query("DELETE FROM users")
    suspend fun clearUsers()
}

// @Dao
interface IdentityKeyDao {
    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentityKey(key: IdentityKeyEntity)

    // @Query("SELECT * FROM identity_keys WHERE userId = :userId")
    suspend fun getIdentityKey(userId: String): IdentityKeyEntity?
}
