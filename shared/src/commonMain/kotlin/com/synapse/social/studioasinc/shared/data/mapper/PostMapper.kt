package com.synapse.social.studioasinc.shared.data.mapper

import com.synapse.social.studioasinc.shared.data.database.Post as DbPost
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.MediaItem
import com.synapse.social.studioasinc.shared.domain.model.PollOption
import com.synapse.social.studioasinc.shared.domain.model.PostMetadata
import com.synapse.social.studioasinc.shared.domain.model.ReactionType
import com.synapse.social.studioasinc.shared.domain.model.MediaType
import com.synapse.social.studioasinc.shared.data.model.PostDto

typealias AppMediaItem = MediaItem
typealias SharedMediaItem = MediaItem
typealias AppPollOption = PollOption
typealias SharedPollOption = PollOption
typealias AppMediaType = MediaType
typealias SharedMediaType = MediaType
typealias AppReactionType = ReactionType
typealias SharedReactionType = ReactionType
typealias AppPostMetadata = PostMetadata
typealias SharedPostMetadata = PostMetadata

object PostMapper {
    
    fun toDto(post: Post): PostDto {
        return PostDto(
            id = post.id,
            key = post.key,
            authorUid = post.authorUid,
            postText = post.postText,
            postImage = post.postImage,
            postType = post.postType,
            postVisibility = post.postVisibility,
            postHideViewsCount = post.postHideViewsCount,
            postHideLikeCount = post.postHideLikeCount,
            postHideCommentsCount = post.postHideCommentsCount,
            postDisableComments = post.postDisableComments,
            publishDate = post.publishDate,
            timestamp = post.timestamp,
            likesCount = post.likesCount,
            commentsCount = post.commentsCount,
            viewsCount = post.viewsCount,
            resharesCount = post.resharesCount,
            mediaItems = post.mediaItems,
            hasPoll = post.hasPoll,
            pollQuestion = post.pollQuestion,
            pollOptions = post.pollOptions,
            pollEndTime = post.pollEndTime,
            pollAllowMultiple = post.pollAllowMultiple,
            hasLocation = post.hasLocation,
            locationName = post.locationName,
            locationAddress = post.locationAddress,
            locationLatitude = post.locationLatitude,
            locationLongitude = post.locationLongitude,
            locationPlaceId = post.locationPlaceId,
            youtubeUrl = post.youtubeUrl,
            metadata = post.metadata
        )
    }
    
    fun toEntity(post: Post): DbPost {
        return DbPost(
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
            encryptedContent = post.encryptedContent,
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
            userPollVote = post.userPollVote,
            metadata = post.metadata?.let { toSharedPostMetadata(it) }
        )
    }

    fun toModel(entity: DbPost): Post {
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
            createdAt = null,
            updatedAt = null,
            likesCount = entity.likesCount,
            commentsCount = entity.commentsCount,
            viewsCount = entity.viewsCount,
            resharesCount = entity.resharesCount,
            mediaItems = entity.mediaItems?.map { item: SharedMediaItem -> toAppMediaItem(item) }?.toMutableList(),
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
            userPollVote = entity.userPollVote,
            metadata = entity.metadata?.let { toAppPostMetadata(it) }
        )
    }

    private fun toSharedMediaItem(item: AppMediaItem): SharedMediaItem {
        return SharedMediaItem(
            id = item.id,
            url = item.url,
            type = SharedMediaType.valueOf(item.type.name),
            thumbnailUrl = item.thumbnailUrl
        )
    }

    private fun toAppMediaItem(item: SharedMediaItem): AppMediaItem {
        return AppMediaItem(
            id = item.id,
            url = item.url,
            type = AppMediaType.valueOf(item.type.name),
            thumbnailUrl = item.thumbnailUrl
        )
    }

    private fun toSharedPollOption(item: AppPollOption): SharedPollOption {
        return SharedPollOption(
            text = item.text,
            votes = item.votes
        )
    }

    private fun toAppPollOption(item: SharedPollOption): AppPollOption {
        return AppPollOption(
            text = item.text,
            votes = item.votes
        )
    }

    private fun toSharedReactionType(type: AppReactionType): SharedReactionType {
        return try {
            SharedReactionType.valueOf(type.name)
        } catch (e: Exception) {
            SharedReactionType.LIKE
        }
    }

    private fun toAppReactionType(type: SharedReactionType): AppReactionType {
        return try {
            AppReactionType.valueOf(type.name)
        } catch (e: Exception) {
            AppReactionType.LIKE
        }
    }

    private fun toSharedPostMetadata(item: AppPostMetadata): SharedPostMetadata {
        return SharedPostMetadata(
            layoutType = item.layoutType,
            backgroundColor = item.backgroundColor
        )
    }

    private fun toAppPostMetadata(item: SharedPostMetadata): AppPostMetadata {
        return AppPostMetadata(
            layoutType = item.layoutType,
            backgroundColor = item.backgroundColor
        )
    }
}
