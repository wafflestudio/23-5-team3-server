package com.snuxi.pot.service

import com.snuxi.participant.entity.Participants
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.pot.*
import com.snuxi.pot.dto.CreatePotResponse
import com.snuxi.pot.dto.PotDto
import com.snuxi.pot.entity.Pots
import com.snuxi.pot.repository.PotRepository
import com.snuxi.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PotService (
    private val potRepository: PotRepository,
    private val participantRepository: ParticipantRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun createPot(
        userId: Long,
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
                ownerId = userId,
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

        updateActivePotIdUsers(listOf(userId), save.id)

        return CreatePotResponse(
            createdPotId = save.id!!
        )
    }

    @Transactional
    fun deletePot(
        userId: Long,
        potId: Long
    ) {
        // 1. 이 사람이 방장이 아니면 error
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()
        if(pot.ownerId != userId) throw NotPotOwnerException()

        // 해당 방에 소속된 모든 유저들의 active pot id를 초기화하기 위함
        val users = participantRepository.findUserIdsByPotId(potId)
        if(users.isNotEmpty()) updateActivePotIdUsers(users, null)

        // 2. 방장이면 방 삭제
        participantRepository.deleteAllByPotId(potId)
        potRepository.deleteById(potId)
    }

    @Transactional
    fun joinPot(userId: Long, potId: Long){
        // 이미 참여한 사람이 또 참여 불가
        if (participantRepository.existsByUserId(userId)) throw DuplicateParticipationException()

        // 팟이 없으면 예외 던짐
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()

        // join 성공 후, participant 정보 업데이트(실패시 예외 던져서 트랜잭션 롤백되게)
        try{
            participantRepository.save(
                Participants(
                    userId = userId,
                    potId = potId,
                    joinedAt = LocalDateTime.now()
                )
            )
        } catch (e: Exception){
            throw TemporarilyNotJoinPotException()
        }

        // 원자적 update 방식 사용
        val updated = potRepository.tryJoinPot(
            potId = potId,
            recruitingStatus = PotStatus.RECRUITING,
            successStatus = PotStatus.SUCCESS
        )
        if(updated == 0) throw PotFullException()

        // user active pot id 또한 업데이트
        updateActivePotIdUsers(listOf(userId), potId)
    }

    @Transactional
    fun leavePot(userId: Long, potId: Long) {
        // 존재하지 않는 팟, 또는 참여 정보가 일치하지 않으면 예외 던짐
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()
        if (!participantRepository.existsByUserIdAndPotId(userId, potId)) throw NotParticipatingException()

        // user active pot id & participant 정보 삭제
        updateActivePotIdUsers(listOf(userId), null)
        participantRepository.deleteByUserIdAndPotId(userId, potId)

        // 원자적 update
        val updated = potRepository.tryLeavePot(
            potId = potId,
            recruitingStatus = PotStatus.RECRUITING,
            successStatus = PotStatus.SUCCESS
        )
        if(updated == 0) throw TemporarilyNotLeavePotException()

        // 업데이트가 정상적으로 적용되었다면(lock 성공), 업데이트된 새로운 방의 정보를 DB 에서 가지고온다
        val updatedPotInfo = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()

        // 방장이 나간 경우
        if(updatedPotInfo.ownerId == userId){
            // 0명 되면 방 삭제
            if(updatedPotInfo.currentCount == 0){
                potRepository.delete(updatedPotInfo)
                return
            }

            // 1명 이상 남아있으면 방장 위임
            val nextOwner = participantRepository.findFirstByPotIdOrderByJoinedAtAsc(potId)
            if(nextOwner != null) updatedPotInfo.ownerId = nextOwner.userId
        }
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
        ).map { PotDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun getMyPot(userId: Long): PotDto? {
        val participation = participantRepository.findByUserId(userId) ?: return null
        val pot = potRepository.findByIdOrNull(participation.potId) ?: return null
        return PotDto.from(pot)
    }

    private fun updateActivePotIdUsers(
        userIds: List<Long>,
        potId: Long?
    ) {
        userRepository.updateActivePotIdForUsers(userIds, potId)
    }
}