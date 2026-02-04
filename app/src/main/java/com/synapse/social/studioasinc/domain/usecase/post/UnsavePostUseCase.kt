package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.usecase.post

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.PostInteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UnsavePostUseCase @Inject constructor(private val repository: PostInteractionRepository) {
    operator fun invoke(postId: String, userId: String): Flow<Result<Unit>> = flow {
        emit(repository.unsavePost(postId, userId))
    }
}
