package com.snuxi.pot.repository

import com.snuxi.pot.PotStatus
import com.snuxi.pot.entity.Pots
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PotRepository : JpaRepository<Pots, Long> {
    fun findAllByDepartureIdAndDestinationIdAndStatusOrderByDepartureTimeAsc(
        departureId: Long,
        destinationId: Long,
        status: PotStatus,
        pageable: Pageable
    ): Page<Pots>
}