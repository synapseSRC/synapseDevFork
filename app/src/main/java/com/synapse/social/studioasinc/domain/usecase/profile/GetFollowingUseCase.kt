package com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.domain.usecase.profile

import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.model.UserProfile
import com.synapse.social.studioasinc.feature.shared.components.feature.search.feature.post.feature.auth.feature.home.domain.model.feature.profile.core.util.feature.inbox.feature.createpost.data.repository.ProfileRepository
import javax.inject.Inject

class GetFollowingUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(userId: String, limit: Int = 20, offset: Int = 0): Result<List<UserProfile>> {
        return repository.getFollowing(userId, limit, offset)
    }
}
