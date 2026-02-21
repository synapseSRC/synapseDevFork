package com.synapse.social.studioasinc.shared.domain.usecase.search

import com.synapse.social.studioasinc.shared.data.repository.SearchRepositoryImpl
import com.synapse.social.studioasinc.shared.domain.model.Post
import com.synapse.social.studioasinc.shared.domain.model.SearchPost
import com.synapse.social.studioasinc.shared.domain.model.SearchHashtag
import com.synapse.social.studioasinc.shared.domain.model.SearchNews
import com.synapse.social.studioasinc.shared.domain.model.SearchAccount

class SearchPostsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(query: String): Result<List<SearchPost>> {
        return searchRepository.searchPosts(query)
    }
}

class SearchHashtagsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(query: String): Result<List<SearchHashtag>> {
        return searchRepository.searchHashtags(query)
    }
}

class GetTrendingHashtagsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(): Result<List<SearchHashtag>> {
        return searchRepository.getTrendingHashtags()
    }
}

class SearchNewsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(query: String): Result<List<SearchNews>> {
        return searchRepository.searchNews(query)
    }
}

class GetSuggestedAccountsUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend operator fun invoke(query: String = ""): Result<List<SearchAccount>> {
        return searchRepository.getSuggestedAccounts(query)
    }
}
