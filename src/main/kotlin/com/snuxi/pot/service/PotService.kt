package com.snuxi.pot.service

import com.snuxi.participant.entity.Participants
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.pot.DuplicateParticipationException
import com.snuxi.pot.InvalidCountException
import com.snuxi.pot.MinMaxReversedException
import com.snuxi.pot.PotStatus
import com.snuxi.pot.dto.CreatePotResponse
import com.snuxi.pot.entity.Pots
import com.snuxi.pot.repository.PotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PotService (
    private val potRepository: PotRepository,
    private val participantRepository: ParticipantRepository
) {
    @Transactional
    fun createPot(
        userId: Long,
        ownerId: Long,
        departureId: Long,
        destinationId: Long,
        departureTime: LocalDateTime,
        minCapacity: Int,
        maxCapacity: Int
    ): CreatePotResponse {
        if(minCapacity > maxCapacity) throw MinMaxReversedException()
        if(minCapacity < 2 || maxCapacity > 4) throw InvalidCountException()
        if(participantRepository.existsByUserId(userId)) throw DuplicateParticipationException()

        val save = potRepository.save(
            Pots(
                ownerId = ownerId,
                departureId = departureId,
                destinationId = destinationId,
                departureTime = departureTime,
                minCapacity = minCapacity,
                maxCapacity = maxCapacity,
                currentCount = 1,
                status = PotStatus.RECRUITING
            )
        )

        participantRepository.save(
            Participants(
                userId = userId,
                potId = save.id!!,
                joinedAt = LocalDateTime.now()
            )
        )

        return CreatePotResponse(
            createdPotId = save.id!!
        )
    }

    @Transactional
    fun deletePot(
        userId: Long,
        potId: Long
    ) {

    }
}