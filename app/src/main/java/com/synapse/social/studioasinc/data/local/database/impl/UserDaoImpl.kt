package com.synapse.social.studioasinc.data.local.database.impl

import com.synapse.social.studioasinc.data.local.database.UserDao
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.database.User as SharedUser
import javax.inject.Inject

class UserDaoImpl @Inject constructor(
    private val db: StorageDatabase
) : UserDao {
    override suspend fun getUserById(userId: String): SharedUser? {
        return db.userQueries.selectById(userId).executeAsOneOrNull()
    }

    override suspend fun insertAll(users: List<SharedUser>) {
        db.transaction {
            users.forEach { user ->
                db.userQueries.insertUser(user)
            }
        }
    }
}
