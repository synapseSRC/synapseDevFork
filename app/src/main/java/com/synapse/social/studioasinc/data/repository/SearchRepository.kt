package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model.SearchResult

interface SearchRepository {
    suspend fun searchUsers(query: String, limit: Int = 20): Result<List<SearchResult.User>>
    suspend fun searchPosts(query: String, limit: Int = 20): Result<List<SearchResult.Post>>
    suspend fun searchMedia(query: String, limit: Int = 20, mediaType: SearchResult.MediaType? = null): Result<List<SearchResult.Media>>
}
