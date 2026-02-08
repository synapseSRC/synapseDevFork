package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.data.local.database.PostEntity
import com.synapse.social.studioasinc.domain.model.Post

object PostMapper {
    fun toEntity(post: Post): PostEntity {
        return PostEntity(id = post.id, authorUid = post.authorUid)
    }

    fun toModel(entity: PostEntity): Post {
        return Post(id = entity.id, authorUid = entity.authorUid ?: "")
    }
}
