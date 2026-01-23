package com.snuxi.pot.repository

import com.snuxi.pot.PotStatus
import com.snuxi.pot.entity.Pots
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PotRepository : JpaRepository<Pots, Long> {
    fun findAllByDepartureIdAndDestinationIdAndStatusOrderByDepartureTimeAsc(
        departureId: Long,
        destinationId: Long,
        status: PotStatus,
        pageable: Pageable
    ): Page<Pots>

    fun findAllByStatusOrderByDepartureTimeAsc(
        status: PotStatus,
        pageable: Pageable
    ): Page<Pots>

    fun findAllByDestinationIdAndStatusOrderByDepartureTimeAsc(
        destinationId: Long,
        status: PotStatus,
        pageable: Pageable
    ): Page<Pots>

    fun findAllByDepartureIdAndStatusOrderByDepartureTimeAsc(
        departureId: Long,
        status: PotStatus,
        pageable: Pageable
    ): Page<Pots>

    // UPDATE query (lock)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
            UPDATE Pots p
            SET p.currentCount = p.currentCount + 1,
                p.status = CASE 
                    WHEN p.currentCount + 1 >= p.maxCapacity THEN :success
                    ELSE p.status
                END 
            WHERE p.id = :potId
            AND p.currentCount < p.maxCapacity
            AND p.status = :recruiting
        """
    )
    fun tryJoinPot(
        @Param("potId") potId: Long,
        @Param("recruiting") recruitingStatus: PotStatus,
        @Param("success") successStatus: PotStatus
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
            UPDATE Pots p
            SET p.currentCount = p.currentCount - 1,
                p.status = CASE 
                    WHEN p.status = :success AND p.currentCount - 1 < p.maxCapacity THEN :recruiting
                    ELSE p.status
                END 
            WHERE p.id = :potId
            AND p.currentCount > 0
        """
    )
    fun tryLeavePot(
        @Param("potId") potId: Long,
        @Param("recruiting") recruitingStatus: PotStatus,
        @Param("success") successStatus: PotStatus
    ): Int
}