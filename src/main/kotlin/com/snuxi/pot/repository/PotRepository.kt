package com.snuxi.pot.repository

import com.snuxi.pot.PotStatus
import com.snuxi.pot.entity.Pots
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PotRepository : JpaRepository<Pots, Long> {
    fun findAllByDepartureIdAndDestinationIdAndStatusInOrderByDepartureTimeAsc (
        departureId: Long,
        destinationId: Long,
        statuses: Collection<PotStatus>,
        pageable: Pageable
    ): Page<Pots>

    fun findAllByStatusInOrderByDepartureTimeAsc(
        statuses: Collection<PotStatus>,
        pageable: Pageable
    ): Page<Pots>

    fun findAllByDestinationIdAndStatusInOrderByDepartureTimeAsc(
        destinationId: Long,
        statuses: Collection<PotStatus>,
        pageable: Pageable
    ): Page<Pots>

    fun findAllByDepartureIdAndStatusInOrderByDepartureTimeAsc(
        departureId: Long,
        statuses: Collection<PotStatus>,
        pageable: Pageable
    ): Page<Pots>

    // UPDATE query (lock)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
    UPDATE Pots p
    SET p.status = CASE 
            WHEN p.currentCount - 1 < p.minCapacity THEN :recruitingStatus
            ELSE p.status
        END,
        p.currentCount = p.currentCount - 1
    WHERE p.id = :potId
    AND p.currentCount > 0
"""
    )
    fun tryLeavePot(
        @Param("potId") potId: Long,
        @Param("recruitingStatus") recruitingStatus: PotStatus
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
    UPDATE Pots p
    SET p.status = CASE 
            WHEN p.currentCount + 1 >= p.minCapacity THEN :successStatus 
            ELSE p.status
        END,
        p.currentCount = p.currentCount + 1
    WHERE p.id = :potId
    AND p.currentCount < p.maxCapacity
"""
    )
    fun tryJoinPot(
        @Param("potId") potId: Long,
        @Param("successStatus") successStatus: PotStatus
    ): Int

    fun findAllByDepartureTimeBetweenAndStatusIn(
        start: LocalDateTime,
        end: LocalDateTime,
        statuses: Collection<PotStatus>
    ): List<Pots>
}