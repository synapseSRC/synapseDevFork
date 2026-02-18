package com.synapse.social.studioasinc.data.local.database

import com.synapse.social.studioasinc.shared.data.database.User as SharedUser

interface UserDao {
    suspend fun getUserById(userId: String): SharedUser?
    suspend fun insertAll(users: List<SharedUser>)
}
