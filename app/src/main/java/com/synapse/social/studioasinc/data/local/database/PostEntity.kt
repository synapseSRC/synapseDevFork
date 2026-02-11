package com.synapse.social.studioasinc.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.synapse.social.studioasinc.domain.model.MediaItem
import com.synapse.social.studioasinc.domain.model.PollOption
import com.synapse.social.studioasinc.domain.model.PostMetadata
import com.synapse.social.studioasinc.domain.model.ReactionType

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val key: String? = null,
    val authorUid: String,
    val postText: String? = null,
    val postImage: String? = null,
    val postType: String? = null,
    val postHideViewsCount: String? = null,
    val postHideLikeCount: String? = null,
    val postHideCommentsCount: String? = null,
    val postDisableComments: String? = null,
    val postVisibility: String? = null,
    val publishDate: String? = null,
    val timestamp: Long,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val viewsCount: Int = 0,
    val resharesCount: Int = 0,
    val mediaItems: List<MediaItem>? = null,
    val isEncrypted: Boolean? = null,
    val encryptedContent: Map<String, String>? = null,
    val nonce: String? = null,
    val encryptionKeyId: String? = null,
    val isDeleted: Boolean? = null,
    val isEdited: Boolean? = null,
    val editedAt: String? = null,
    val deletedAt: String? = null,
    val hasPoll: Boolean? = null,
    val pollQuestion: String? = null,
    val pollOptions: List<PollOption>? = null,
    val pollEndTime: String? = null,
    val pollAllowMultiple: Boolean? = null,
    val hasLocation: Boolean? = null,
    val locationName: String? = null,
    val locationAddress: String? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val locationPlaceId: String? = null,
    val youtubeUrl: String? = null,
    val reactions: Map<ReactionType, Int>? = null,
    val userReaction: ReactionType? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val metadata: PostMetadata? = null
)
