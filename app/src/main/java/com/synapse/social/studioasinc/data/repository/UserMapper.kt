package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.core.network.SupabaseClient
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.local.database.UserEntity
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.User

object UserMapper {

    fun toEntity(user: User): UserEntity {
        return UserEntity(
            uid = user.uid,
            username = user.username,
            email = user.email,
            avatarUrl = user.avatar,
            isVerified = user.verify
        )
    }

    fun toModel(entity: UserEntity): User {
        return User(
            uid = entity.uid,
            username = entity.username,
            email = entity.email,
            avatar = entity.avatarUrl?.let { url ->
                SupabaseClient.constructAvatarUrl(url)
            },
            verify = entity.isVerified
        )
    }
}
