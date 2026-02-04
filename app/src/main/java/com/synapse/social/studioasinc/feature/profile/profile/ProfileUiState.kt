package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.feature.profile.profile

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.model.UserProfile

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String, val exception: Throwable? = null) : ProfileUiState()
    object Empty : ProfileUiState()
}
