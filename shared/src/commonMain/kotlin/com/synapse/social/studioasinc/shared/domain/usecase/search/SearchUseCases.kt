package com.synapse.social.studioasinc.shared.domain.usecase.search

import com.synapse.social.studioasinc.shared.domain.model.*
import com.synapse.social.studioasinc.shared.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchPostsUseCase(private val repository: PostRepository) {
    operator fun invoke(query: String): Flow<Result<List<Post>>> = flow {
        // Simple search logic for now
        emit(Result.success(emptyList()))
    }
}

class SearchUsersUseCase(private val repository: UserRepository) {
    operator fun invoke(query: String): Flow<Result<List<UserProfile>>> = flow {
        // Simple search logic for now
        emit(Result.success(emptyList()))
    }
}

class GetSearchHistoryUseCase(private val repository: ISearchRepository) {
    operator fun invoke(): Flow<Result<List<SearchModels.SearchHistory>>> = flow {
        emit(repository.getSearchHistory())
    }
}

class SaveSearchQueryUseCase(private val repository: ISearchRepository) {
    suspend operator fun invoke(query: String) {
        repository.saveSearchQuery(query)
    }
}

class ClearSearchHistoryUseCase(private val repository: ISearchRepository) {
    suspend operator fun invoke() {
        repository.clearSearchHistory()
    }
}
