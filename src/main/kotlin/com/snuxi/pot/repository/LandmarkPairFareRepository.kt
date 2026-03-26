package com.snuxi.pot.repository

import com.snuxi.pot.model.LandmarkPairFare
import com.snuxi.pot.model.LandmarkPairFareId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LandmarkPairFareRepository : JpaRepository<LandmarkPairFare, LandmarkPairFareId> {
    fun findByDepartureIdAndDestinationId(departureId: Long, destinationId: Long): LandmarkPairFare?
}
