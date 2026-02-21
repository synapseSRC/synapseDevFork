package com.synapse.social.studioasinc.shared.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class PaginationManager<T>(
    private val pageSize: Int = 20,
    private val maxCachedItems: Int = 200,
    private val onLoadPage: suspend (page: Int, pageSize: Int) -> Result<List<T>>,
    private val onError: (String) -> Unit,
    private val coroutineScope: CoroutineScope
) {
    private val _paginationState = MutableStateFlow<PaginationState<T>>(PaginationState.Initial)
    val paginationState: StateFlow<PaginationState<T>> = _paginationState.asStateFlow()

    private var currentPage = 0
    private var isLoading = false
    private var hasMoreData = true
    private val loadedItems = mutableListOf<T>()
    private var loadJob: Job? = null

    sealed class PaginationState<out T> {
        object Initial : PaginationState<Nothing>()
        object Refreshing : PaginationState<Nothing>()
        data class LoadingMore<T>(val currentItems: List<T>) : PaginationState<T>()
        data class Success<T>(val items: List<T>, val hasMore: Boolean) : PaginationState<T>()
        data class Error<T>(val message: String, val currentItems: List<T>) : PaginationState<T>()
        data class EndOfList<T>(val items: List<T>) : PaginationState<T>()
    }

    fun loadNextPage() {
        if (isLoading || !hasMoreData) return
        
        loadJob?.cancel()
        loadJob = coroutineScope.launch {
            try {
                isLoading = true
                _paginationState.value = PaginationState.LoadingMore(loadedItems.toList())
                
                val result = onLoadPage(currentPage, pageSize)
                result.fold(
                    onSuccess = { newItems ->
                        loadedItems.addAll(newItems)
                        enforceItemLimit()
                        currentPage++
                        hasMoreData = newItems.size == pageSize
                        
                        _paginationState.value = if (hasMoreData) {
                            PaginationState.Success(loadedItems.toList(), true)
                        } else {
                            PaginationState.EndOfList(loadedItems.toList())
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Unknown error"
                        onError(errorMessage)
                        _paginationState.value = PaginationState.Error(errorMessage, loadedItems.toList())
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                onError(errorMessage)
                _paginationState.value = PaginationState.Error(errorMessage, loadedItems.toList())
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = coroutineScope.launch {
            try {
                _paginationState.value = PaginationState.Refreshing
                currentPage = 0
                hasMoreData = true
                loadedItems.clear()
                
                val result = onLoadPage(0, pageSize)
                result.fold(
                    onSuccess = { newItems ->
                        loadedItems.addAll(newItems)
                        currentPage = 1
                        hasMoreData = newItems.size == pageSize
                        
                        _paginationState.value = if (hasMoreData) {
                            PaginationState.Success(loadedItems.toList(), true)
                        } else {
                            PaginationState.EndOfList(loadedItems.toList())
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Unknown error"
                        onError(errorMessage)
                        _paginationState.value = PaginationState.Error(errorMessage, emptyList())
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                onError(errorMessage)
                _paginationState.value = PaginationState.Error(errorMessage, emptyList())
            }
        }
    }

    private fun enforceItemLimit() {
        if (loadedItems.size > maxCachedItems) {
            val itemsToRemove = loadedItems.size - maxCachedItems
            repeat(itemsToRemove) {
                loadedItems.removeFirstOrNull()
            }
        }
    }

    fun reset() {
        loadJob?.cancel()
        currentPage = 0
        isLoading = false
        hasMoreData = true
        loadedItems.clear()
        _paginationState.value = PaginationState.Initial
    }

    fun getCurrentItems(): List<T> = loadedItems.toList()
    fun isAtEnd(): Boolean = !hasMoreData
}
