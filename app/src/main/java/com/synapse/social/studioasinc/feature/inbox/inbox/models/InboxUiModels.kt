package com.synapse.social.studioasinc.ui.inbox.models

/**
 * Sealed interface representing the different states of the inbox screen.
 */
sealed interface InboxUiState {
    /**
     * Loading state
     */
    data object Loading : InboxUiState

    /**
     * Success state (empty as functionality is removed)
     */
    data object Success : InboxUiState

    /**
     * Error state with message
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : InboxUiState
}

/**
 * Enum for inbox tabs
 */
enum class InboxTab(val title: String, val icon: String) {
    CHATS("Chats", "chat"),
    CALLS("Calls", "call"),
    CONTACTS("Contacts", "contacts")
}

/**
 * Empty state types for different tabs
 */
enum class EmptyStateType {
    CHATS,
    CALLS,
    CONTACTS,
    SEARCH_NO_RESULTS,
    ARCHIVED,
    ERROR
}

/**
 * User action events from inbox screen
 */
sealed interface InboxAction {
    data object Refresh : InboxAction
}
