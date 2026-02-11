package com.synapse.social.studioasinc.data.local.database

import androidx.room.TypeConverter
import com.synapse.social.studioasinc.domain.model.MediaItem
import com.synapse.social.studioasinc.domain.model.PollOption
import com.synapse.social.studioasinc.domain.model.PostMetadata
import com.synapse.social.studioasinc.domain.model.ReactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromMediaItemList(value: List<MediaItem>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMediaItemList(value: String?): List<MediaItem>? {
        return value?.let {
            if (it.isBlank()) emptyList() else try {
                json.decodeFromString<List<MediaItem>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromPollOptionList(value: List<PollOption>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toPollOptionList(value: String?): List<PollOption>? {
        return value?.let {
            if (it.isBlank()) emptyList() else try {
                json.decodeFromString<List<PollOption>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromPostMetadata(value: PostMetadata?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toPostMetadata(value: String?): PostMetadata? {
        return value?.let {
            if (it.isBlank()) null else try {
                json.decodeFromString<PostMetadata>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromReactionMap(value: Map<ReactionType, Int>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toReactionMap(value: String?): Map<ReactionType, Int>? {
        return value?.let {
            if (it.isBlank()) emptyMap() else try {
                json.decodeFromString<Map<ReactionType, Int>>(it)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    @TypeConverter
    fun fromReactionType(value: ReactionType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toReactionType(value: String?): ReactionType? {
        return value?.let {
            try {
                ReactionType.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let {
            if (it.isBlank()) emptyMap() else try {
                json.decodeFromString<Map<String, String>>(it)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            if (it.isBlank()) emptyList() else try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
