package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.database.User as SharedUser
import com.synapse.social.studioasinc.domain.model.User

object UserMapper {

    fun toEntity(user: User): SharedUser {
        return SharedUser(
            id = user.uid,
            username = user.username,
            email = user.email,
            fullName = null, // Not present in Domain User
            avatarUrl = user.avatar,
            bio = null, // Not present in Domain User
            website = null,
            location = null,
            isVerified = user.verify,
            followersCount = 0,
            followingCount = 0,
            postsCount = 0
        )
    }

    fun toModel(entity: SharedUser): User {
        return User(
            uid = entity.id,
            username = entity.username,
            email = entity.email,
            avatar = entity.avatarUrl?.let { url ->
                if (url.startsWith("http")) url else SupabaseClient.constructAvatarUrl(url)
            },
            verify = entity.isVerified
        )
    }
}
