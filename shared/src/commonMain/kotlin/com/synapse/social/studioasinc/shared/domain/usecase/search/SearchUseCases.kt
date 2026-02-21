package com.synapse.social.studioasinc.shared.domain.usecase.search

import com.synapse.social.studioasinc.shared.data.repository.SearchRepositoryImpl
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.SearchModels

class SearchPostsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(query: String): Result<List<Post>> {
        return searchRepository.searchPosts(query)
    }
}

class SearchHashtagsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(query: String): Result<List<SearchModels.SearchHashtag>> {
        return searchRepository.searchHashtags(query)
    }
}

class GetTrendingHashtagsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(): Result<List<SearchModels.SearchHashtag>> {
        return searchRepository.getTrendingHashtags()
    }
}

class SearchNewsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(query: String): Result<List<SearchModels.SearchNews>> {
        return searchRepository.searchNews(query)
    }
}

class GetSuggestedAccountsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(): Result<List<SearchModels.SearchAccount>> {
        return searchRepository.getSuggestedAccounts()
    }
}
