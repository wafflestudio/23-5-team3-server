package com.snuxi.participant.repository

import com.snuxi.participant.entity.Participants
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipantRepository : JpaRepository<Participants, Long>{
    fun existsByUserId(userId: Long): Boolean
    fun findByUserId(userId: Long): Participants?
    fun deleteByUserIdAndPotId(userId: Long, potId: Long)
}