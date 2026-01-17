package com.snuxi.participant.repository

import com.snuxi.participant.entity.Participants
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ParticipantRepository : JpaRepository<Participants, Long>{
    fun existsByUserId(userId: Long): Boolean
    fun findByUserId(userId: Long): Participants?
    fun deleteByUserIdAndPotId(userId: Long, potId: Long)
    fun deleteAllByPotId(potId: Long)

    @Query(
        value = "SELECT user_id FROM participants WHERE pot_id = :potId",
        nativeQuery = true
    )
    fun findUserIdsByPotId(
        potId: Long
    ): List<Long>

    fun findFirstByPotIdOrderByJoinedAtAsc(potId: Long): Participants?
}