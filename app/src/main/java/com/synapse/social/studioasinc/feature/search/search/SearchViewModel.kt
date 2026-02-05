package com.synapse.social.studioasinc.ui.search

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.shared.domain.model.SearchAccount
import com.synapse.social.studioasinc.shared.domain.model.SearchHashtag
import com.synapse.social.studioasinc.shared.domain.model.SearchNews
import com.synapse.social.studioasinc.shared.domain.model.SearchPost
import com.synapse.social.studioasinc.shared.domain.usecase.search.GetSuggestedAccountsUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.search.SearchHashtagsUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.search.SearchNewsUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.search.SearchPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchTab(val title: String) {
    POSTS("Posts"),
    HASHTAGS("Hashtags"),
    NEWS("News"),
    FOR_YOU("For you")
}

data class SearchUiState(
    val query: String = "",
    val active: Boolean = false,
    val isLoading: Boolean = false,
    val selectedTab: SearchTab = SearchTab.POSTS,
    val error: String? = null,
    val posts: List<SearchPost> = emptyList(),
    val hashtags: List<SearchHashtag> = emptyList(),
    val news: List<SearchNews> = emptyList(),
    val accounts: List<SearchAccount> = emptyList(),
    val searchHistory: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchPostsUseCase: SearchPostsUseCase,
    private val searchHashtagsUseCase: SearchHashtagsUseCase,
    private val searchNewsUseCase: SearchNewsUseCase,
    private val getSuggestedAccountsUseCase: GetSuggestedAccountsUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val HISTORY_KEY = "search_history"

    init {
        loadHistory()
        // Initial load for "For You" or default content
        refreshCurrentTab()
    }

    private fun loadHistory() {
        val historyString = sharedPreferences.getString(HISTORY_KEY, "") ?: ""
        val historyList = if (historyString.isNotEmpty()) {
            historyString.split(",").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
        _uiState.update { it.copy(searchHistory = historyList) }
    }

    private fun saveHistory(history: List<String>) {
        val historyString = history.joinToString(",")
        sharedPreferences.edit().putString(HISTORY_KEY, historyString).apply()
    }

    fun addToHistory(query: String) {
        if (query.isBlank()) return
        val currentHistory = uiState.value.searchHistory.toMutableList()
        currentHistory.remove(query)
        currentHistory.add(0, query)
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }
        _uiState.update { it.copy(searchHistory = currentHistory) }
        saveHistory(currentHistory)
    }

    fun removeFromHistory(query: String) {
        val currentHistory = uiState.value.searchHistory.toMutableList()
        currentHistory.remove(query)
        _uiState.update { it.copy(searchHistory = currentHistory) }
        saveHistory(currentHistory)
    }

    fun clearHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
        saveHistory(emptyList())
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            if (query.isNotEmpty() || uiState.value.selectedTab == SearchTab.HASHTAGS || uiState.value.selectedTab == SearchTab.FOR_YOU) {
                performSearch(query)
            }
        }
    }

    fun onActiveChange(active: Boolean) {
        _uiState.update { it.copy(active = active) }
    }

    fun onTabSelected(tab: SearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        performSearch(uiState.value.query)
    }

    fun onSearch(query: String) {
         _uiState.update { it.copy(query = query) }
         addToHistory(query)
         performSearch(query)
    }

    fun clearSearch() {
        _uiState.update { it.copy(query = "") }
        performSearch("")
    }

    private fun performSearch(query: String) {
        refreshCurrentTab(query)
    }

    fun refreshCurrentTab(query: String = uiState.value.query) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (uiState.value.selectedTab) {
                    SearchTab.POSTS -> {
                         val result = searchPostsUseCase(query)
                         result.onSuccess { data -> _uiState.update { it.copy(posts = data, isLoading = false) } }
                         result.onFailure { err -> _uiState.update { it.copy(error = err.message, isLoading = false) } }
                    }
                    SearchTab.HASHTAGS -> {
                        val result = searchHashtagsUseCase(query)
                        result.onSuccess { data -> _uiState.update { it.copy(hashtags = data, isLoading = false) } }
                        result.onFailure { err -> _uiState.update { it.copy(error = err.message, isLoading = false) } }
                    }
                    SearchTab.NEWS -> {
                        val result = searchNewsUseCase(query)
                        result.onSuccess { data -> _uiState.update { it.copy(news = data, isLoading = false) } }
                        result.onFailure { err -> _uiState.update { it.copy(error = err.message, isLoading = false) } }
                    }
                    SearchTab.FOR_YOU -> {
                        val result = getSuggestedAccountsUseCase(query)
                        result.onSuccess { data -> _uiState.update { it.copy(accounts = data, isLoading = false) } }
                        result.onFailure { err -> _uiState.update { it.copy(error = err.message, isLoading = false) } }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
