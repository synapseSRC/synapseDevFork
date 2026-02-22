package com.synapse.social.studioasinc.shared.domain.repository

import com.synapse.social.studioasinc.shared.domain.model.LocationData

interface LocationRepository {
    suspend fun searchLocations(query: String): Result<List<LocationData>>
}
