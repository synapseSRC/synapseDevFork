package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.core.network.SupabaseClient
import com.synapse.social.studioasinc.shared.data.database.User as DbUser
import com.synapse.social.studioasinc.domain.model.User as DomainUser

object UserMapper {

    fun toEntity(user: DomainUser): DbUser {
        return DbUser(
            id = user.uid,
            username = user.username ?: "",
            fullName = user.displayName,
            avatarUrl = user.avatar,
            bio = user.bio,
            website = null,
            location = null,
            isVerified = user.verify,
            followersCount = user.followersCount,
            followingCount = user.followingCount,
            postsCount = user.postsCount
        )
    }

    fun toModel(entity: DbUser): DomainUser {
        return DomainUser(
            uid = entity.id,
            username = entity.username,
            displayName = entity.fullName,
            email = null,
            avatar = entity.avatarUrl?.let { url ->
                if (url.startsWith("http")) url else SupabaseClient.constructAvatarUrl(url)
            },
            verify = entity.isVerified,
            bio = entity.bio,
            followersCount = entity.followersCount,
            followingCount = entity.followingCount,
            postsCount = entity.postsCount
        )
    }
}
