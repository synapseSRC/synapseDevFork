package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.usecase.profile

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.ProfileActionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MuteUserUseCase @Inject constructor(
    private val repository: ProfileActionRepository
) {
    operator fun invoke(userId: String, mutedUserId: String): Flow<Result<Unit>> = flow {
        emit(repository.muteUser(userId, mutedUserId))
    }
}
