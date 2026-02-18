package com.synapse.social.studioasinc.shared.data.local.database

import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.database.User

class UserDaoImpl(private val database: StorageDatabase) : UserDao {

    private fun mapToUser(user: UserEntity): User {
        return User(
            id = user.uid,
            username = user.username ?: "",
            email = user.email,
            fullName = null,
            avatarUrl = user.avatarUrl,
            bio = null,
            website = null,
            location = null,
            isVerified = user.isVerified,
            followersCount = 0,
            followingCount = 0,
            postsCount = 0
        )
    }

    override suspend fun insertUser(user: UserEntity) {
        database.userQueries.insertUser(mapToUser(user))
    }

    override suspend fun insertAll(users: List<UserEntity>) {
        database.userQueries.transaction {
            users.forEach { user ->
                database.userQueries.insertUser(mapToUser(user))
            }
        }
    }

    override suspend fun getUserById(userId: String): UserEntity? {
        return database.userQueries.selectById(userId).executeAsOneOrNull()?.let {
            UserEntity(
                uid = it.id,
                username = it.username,
                email = it.email,
                avatarUrl = it.avatarUrl,
                isVerified = it.isVerified
            )
        }
    }

    override suspend fun clearUsers() {
        database.userQueries.deleteAll()
    }
}
