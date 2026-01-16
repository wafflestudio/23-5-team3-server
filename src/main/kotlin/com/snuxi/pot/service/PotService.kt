package com.snuxi.pot.service

import com.snuxi.participant.entity.Participants
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.pot.DuplicateParticipationException
import com.snuxi.pot.InvalidCountException
import com.snuxi.pot.MinMaxReversedException
import com.snuxi.pot.NotParticipatingException
import com.snuxi.pot.PotFullException
import com.snuxi.pot.PotNotFoundException
import com.snuxi.pot.PotStatus
import com.snuxi.pot.dto.CreatePotResponse
import com.snuxi.pot.dto.PotDto
import com.snuxi.pot.entity.Pots
import com.snuxi.pot.repository.PotRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
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
                estimatedFee = 0,
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

    @Transactional
    fun joinPot(userId: Long, potId: Long){
        if (participantRepository.existsByUserId(userId)) throw DuplicateParticipationException()

        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()
        if (pot.currentCount >= pot.maxCapacity) throw PotFullException()

        participantRepository.save(
            Participants(
                userId = userId,
                potId = potId,
                joinedAt = LocalDateTime.now()
            )
        )

        pot.currentCount += 1
    }

    @Transactional
    fun leavePot(userId: Long, potId: Long) {
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()

        if (!participantRepository.existsByUserId(userId)) throw NotParticipatingException()

        participantRepository.deleteByUserIdAndPotId(userId, potId)

        if (pot.currentCount > 0) {
            pot.currentCount -= 1
        }

    //TODO 방장(ownerId) 나가는 경우 처리

    }

    @Transactional(readOnly = true)
    fun searchPots(
        departureId: Long,
        destinationId: Long,
        pageable: Pageable
    ): Page<PotDto> {
        return potRepository.findAllByDepartureIdAndDestinationIdAndStatusOrderByDepartureTimeAsc(
            departureId,
            destinationId,
            PotStatus.RECRUITING,
            pageable
        ).map {PotDto.from(it)}
    }

    @Transactional(readOnly = true)
    fun getMyPot(userId: Long): PotDto? {
        val participation = participantRepository.findByUserId(userId) ?: return null
        val pot = potRepository.findByIdOrNull(participation.potId) ?: return null
        return PotDto.from(pot)
    }

}