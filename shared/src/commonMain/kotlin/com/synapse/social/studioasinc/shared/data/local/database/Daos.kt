package com.synapse.social.studioasinc.shared.data.local.database







interface UserDao {

    suspend fun insertUser(user: UserEntity)


    suspend fun getUser(uid: String): UserEntity?


    suspend fun clearUsers()
}


interface IdentityKeyDao {

    suspend fun insertIdentityKey(key: IdentityKeyEntity)


    suspend fun getIdentityKey(userId: String): IdentityKeyEntity?
}
