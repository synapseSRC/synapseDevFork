package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.SearchModels
import kotlinx.coroutines.flow.Flow

interface ISearchRepository {
    suspend fun getSearchHistory(): Result<List<SearchModels.SearchHistory>>
    suspend fun saveSearchQuery(query: String)
    suspend fun clearSearchHistory()
}
