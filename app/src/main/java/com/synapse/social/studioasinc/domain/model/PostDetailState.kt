package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.model

/**
 * Sealed class representing the state of post detail loading.
 * Used by PostDetailViewModel to manage UI state.
 *
 * Requirements: 1.1
 */
sealed class PostDetailState {
    /**
     * Initial loading state
     */
    object Loading : PostDetailState()

    /**
     * Successfully loaded post detail
     */
    data class Success(val postDetail: PostDetail) : PostDetailState()

    /**
     * Error occurred while loading post detail
     */
    data class Error(val message: String, val throwable: Throwable? = null) : PostDetailState()

    /**
     * Post not found
     */
    object NotFound : PostDetailState()
}
