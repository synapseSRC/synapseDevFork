package com.synapse.social.studioasinc.shared.domain.usecase

import com.synapse.social.studioasinc.shared.domain.model.FamilyConnection
import com.synapse.social.studioasinc.shared.domain.repository.ProfileSectionsRepository
import kotlinx.coroutines.flow.Flow

class GetFamilyConnectionsUseCase(
    private val repository: ProfileSectionsRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<FamilyConnection>>> {
        return repository.getFamilyConnections(userId)
    }
}

class AddFamilyConnectionUseCase(
    private val repository: ProfileSectionsRepository
) {
    suspend operator fun invoke(userId: String, connection: FamilyConnection): Result<Unit> {
        return repository.addFamilyConnection(userId, connection)
    }
}

class RemoveFamilyConnectionUseCase(
    private val repository: ProfileSectionsRepository
) {
    suspend operator fun invoke(connectionId: String): Result<Unit> {
        return repository.removeFamilyConnection(connectionId)
    }
}
