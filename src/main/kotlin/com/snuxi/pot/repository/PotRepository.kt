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
    @Query("""
        SELECT p FROM Pots p 
        WHERE p.departureId = :depId AND p.destinationId = :destId 
        AND p.status IN :statuses 
        AND p.currentCount < p.maxCapacity
        ORDER BY p.departureTime ASC
    """)
    fun findAvailableWithDest(
        @Param("depId") departureId: Long,
        @Param("destId") destinationId: Long,
        @Param("statuses") statuses: Collection<PotStatus>,
        pageable: Pageable
    ): Page<Pots>

    @Query("""
        SELECT p FROM Pots p 
        WHERE p.status IN :statuses 
        AND p.currentCount < p.maxCapacity
        ORDER BY p.departureTime ASC
    """)
    fun findAvailableAll(
        @Param("statuses") statuses: Collection<PotStatus>,
        pageable: Pageable
    ): Page<Pots>

    @Query("""
        SELECT p FROM Pots p 
        WHERE p.destinationId = :destId 
        AND p.status IN :statuses 
        AND p.currentCount < p.maxCapacity
        ORDER BY p.departureTime ASC
    """)
    fun findAvailableByDestination(
        @Param("destId") destinationId: Long,
        @Param("statuses") statuses: Collection<PotStatus>,
        pageable: Pageable
    ): Page<Pots>

    @Query("""
        SELECT p FROM Pots p 
        WHERE p.departureId = :depId 
        AND p.status IN :statuses 
        AND p.currentCount < p.maxCapacity
        ORDER BY p.departureTime ASC
    """)
    fun findAvailableByDeparture(
        @Param("depId") departureId: Long,
        @Param("statuses") statuses: Collection<PotStatus>,
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