package com.synapse.social.studioasinc.data.repository

import com.synapse.social.studioasinc.shared.data.database.Post as SharedPost
import com.synapse.social.studioasinc.domain.model.Post
import com.synapse.social.studioasinc.domain.model.MediaItem
import com.synapse.social.studioasinc.domain.model.PollOption
import com.synapse.social.studioasinc.domain.model.PostMetadata
import com.synapse.social.studioasinc.domain.model.ReactionType
import com.synapse.social.studioasinc.domain.model.MediaType
import com.synapse.social.studioasinc.domain.model.FeelingActivity
import com.synapse.social.studioasinc.domain.model.FeelingType
import com.synapse.social.studioasinc.shared.domain.model.MediaItem as SharedMediaItem
import com.synapse.social.studioasinc.shared.domain.model.PollOption as SharedPollOption
import com.synapse.social.studioasinc.shared.domain.model.PostMetadata as SharedPostMetadata
import com.synapse.social.studioasinc.shared.domain.model.ReactionType as SharedReactionType
import com.synapse.social.studioasinc.shared.domain.model.MediaType as SharedMediaType
import com.synapse.social.studioasinc.shared.domain.model.FeelingActivity as SharedFeelingActivity
import com.synapse.social.studioasinc.shared.domain.model.FeelingType as SharedFeelingType

object PostMapper {
    fun toEntity(post: Post): SharedPost {
        return SharedPost(
            id = post.id,
            key = post.key,
            authorUid = post.authorUid,
            postText = post.postText,
            postImage = post.postImage,
            postType = post.postType,
            postHideViewsCount = post.postHideViewsCount,
            postHideLikeCount = post.postHideLikeCount,
            postHideCommentsCount = post.postHideCommentsCount,
            postDisableComments = post.postDisableComments,
            postVisibility = post.postVisibility,
            publishDate = post.publishDate,
            timestamp = post.timestamp,
            likesCount = post.likesCount,
            commentsCount = post.commentsCount,
            viewsCount = post.viewsCount,
            resharesCount = post.resharesCount,
            mediaItems = post.mediaItems?.map { toSharedMediaItem(it) },
            isEncrypted = post.isEncrypted,
            nonce = post.nonce,
            encryptionKeyId = post.encryptionKeyId,
            isDeleted = post.isDeleted,
            isEdited = post.isEdited,
            editedAt = post.editedAt,
            deletedAt = post.deletedAt,
            hasPoll = post.hasPoll,
            pollQuestion = post.pollQuestion,
            pollOptions = post.pollOptions?.map { toSharedPollOption(it) },
            pollEndTime = post.pollEndTime,
            pollAllowMultiple = post.pollAllowMultiple,
            hasLocation = post.hasLocation,
            locationName = post.locationName,
            locationAddress = post.locationAddress,
            locationLatitude = post.locationLatitude,
            locationLongitude = post.locationLongitude,
            locationPlaceId = post.locationPlaceId,
            youtubeUrl = post.youtubeUrl,
            reactions = post.reactions?.mapKeys { toSharedReactionType(it.key) },
            userReaction = post.userReaction?.let { toSharedReactionType(it) },
            username = post.username,
            avatarUrl = post.avatarUrl,
            isVerified = post.isVerified,
            metadata = post.metadata?.let { toSharedPostMetadata(it) },
            userPollVote = post.userPollVote
        )
    }

    fun toModel(entity: SharedPost): Post {
        return Post(
            id = entity.id,
            key = entity.key,
            authorUid = entity.authorUid,
            postText = entity.postText,
            postImage = entity.postImage,
            postType = entity.postType,
            postHideViewsCount = entity.postHideViewsCount,
            postHideLikeCount = entity.postHideLikeCount,
            postHideCommentsCount = entity.postHideCommentsCount,
            postDisableComments = entity.postDisableComments,
            postVisibility = entity.postVisibility,
            publishDate = entity.publishDate,
            timestamp = entity.timestamp,
            likesCount = entity.likesCount,
            commentsCount = entity.commentsCount,
            viewsCount = entity.viewsCount,
            resharesCount = entity.resharesCount,
            mediaItems = entity.mediaItems?.map { toAppMediaItem(it) }?.toMutableList(),
            isEncrypted = entity.isEncrypted,
            encryptedContent = null,
            nonce = entity.nonce,
            encryptionKeyId = entity.encryptionKeyId,
            isDeleted = entity.isDeleted,
            isEdited = entity.isEdited,
            editedAt = entity.editedAt,
            deletedAt = entity.deletedAt,
            hasPoll = entity.hasPoll,
            pollQuestion = entity.pollQuestion,
            pollOptions = entity.pollOptions?.map { toAppPollOption(it) },
            pollEndTime = entity.pollEndTime,
            pollAllowMultiple = entity.pollAllowMultiple,
            hasLocation = entity.hasLocation,
            locationName = entity.locationName,
            locationAddress = entity.locationAddress,
            locationLatitude = entity.locationLatitude,
            locationLongitude = entity.locationLongitude,
            locationPlaceId = entity.locationPlaceId,
            youtubeUrl = entity.youtubeUrl,
            reactions = entity.reactions?.mapKeys { toAppReactionType(it.key) },
            userReaction = entity.userReaction?.let { toAppReactionType(it) },
            username = entity.username,
            avatarUrl = entity.avatarUrl,
            isVerified = entity.isVerified,
            metadata = entity.metadata?.let { toAppPostMetadata(it) },
            userPollVote = entity.userPollVote
        )
    }

    private fun toSharedMediaItem(item: MediaItem): SharedMediaItem {
        return SharedMediaItem(
            id = item.id,
            url = item.url,
            type = try { SharedMediaType.valueOf(item.type.name) } catch (e: Exception) { SharedMediaType.IMAGE },
            thumbnailUrl = item.thumbnailUrl,
            // aspectRatio property is missing in SharedMediaItem according to file read, but error log complained about it.
            // Wait, previous error: "No parameter with name 'aspectRatio' found".
            // So SharedMediaItem does NOT have aspectRatio.
            // I should remove it.
            // Error log: e: .../PostMapper.kt:135:13 No parameter with name 'aspectRatio' found.
            // So I remove it here.
            duration = item.duration,
            size = item.size,
            mimeType = item.mimeType
        )
    }

    private fun toAppMediaItem(item: SharedMediaItem): MediaItem {
        return MediaItem(
            id = item.id,
            url = item.url,
            type = try { MediaType.valueOf(item.type.name) } catch (e: Exception) { MediaType.IMAGE },
            thumbnailUrl = item.thumbnailUrl,
            aspectRatio = null, // Set default or null
            duration = item.duration,
            size = item.size,
            mimeType = item.mimeType
        )
    }

    private fun toSharedPollOption(item: PollOption): SharedPollOption {
        return SharedPollOption(
            text = item.text,
            votes = item.votes
        )
    }

    private fun toAppPollOption(item: SharedPollOption): PollOption {
        return PollOption(
            text = item.text,
            votes = item.votes
        )
    }

    private fun toSharedReactionType(item: ReactionType): SharedReactionType {
        return try {
            SharedReactionType.valueOf(item.name)
        } catch (e: Exception) {
            SharedReactionType.LIKE
        }
    }

    private fun toAppReactionType(item: SharedReactionType): ReactionType {
        return try {
            ReactionType.valueOf(item.name)
        } catch (e: Exception) {
            ReactionType.LIKE
        }
    }

    private fun toSharedPostMetadata(item: PostMetadata): SharedPostMetadata {
        return SharedPostMetadata(
            layoutType = item.layoutType,
            backgroundColor = item.backgroundColor,
            feeling = item.feeling?.let {
                SharedFeelingActivity(
                    emoji = it.emoji,
                    text = it.text,
                    type = try { SharedFeelingType.valueOf(it.type.name) } catch(e: Exception) { SharedFeelingType.MOOD }
                )
            },
            taggedPeople = null // Mapping User list might be complex and not needed for basic migration yet, or map simple properties
        )
    }

    private fun toAppPostMetadata(item: SharedPostMetadata): PostMetadata {
        return PostMetadata(
            layoutType = item.layoutType,
            backgroundColor = item.backgroundColor,
            feeling = item.feeling?.let {
                FeelingActivity(
                    emoji = it.emoji,
                    text = it.text,
                    type = try { FeelingType.valueOf(it.type.name) } catch(e: Exception) { FeelingType.MOOD }
                )
            },
            taggedPeople = null
        )
    }
}
