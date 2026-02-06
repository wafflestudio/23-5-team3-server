package com.snuxi.pot.service

import com.snuxi.notification.service.PushService
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.chat.service.ChatBotService
import com.snuxi.participant.entity.Participants
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.pot.*
import com.snuxi.pot.dto.CreatePotResponse
import com.snuxi.pot.dto.PotDto
import com.snuxi.pot.dto.core.LandmarkDto
import com.snuxi.pot.dto.response.PotParticipantResponse
import com.snuxi.pot.entity.Pots
import com.snuxi.pot.repository.LandmarkRepository
import com.snuxi.pot.repository.PotRepository
import com.snuxi.user.UserNotFoundException
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
    private val userRepository: UserRepository,
    private val pushService: PushService,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatBotService: ChatBotService,
    private val landmarkRepository: LandmarkRepository
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

        // 챗봇 메시지 전송
        val username = userRepository.findById(userId).orElseThrow {
            UserNotFoundException()
        }.username
        chatBotService.sendJoinMsg(save.id!!, userId, username)

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

        val notifyTargetIds = users.filter { it != userId }
        if (notifyTargetIds.isNotEmpty()) {
            pushService.sendNotificationToUsers(
                notifyTargetIds,
                "SNUXI 팟 취소",
                "참여 중이던 팟이 방장에 의해 취소되었습니다."
            )
        }

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
        } catch (_: Exception){
            throw TemporarilyNotJoinPotException()
        }

        // 원자적 update 방식 사용
        val updated = potRepository.tryJoinPot(
            potId = potId,
            successStatus = PotStatus.SUCCESS
        )
        if (updated == 0) throw PotFullException()

        //업뎃 후 팟 상태 확인하고 SUCCESS 시 알림 발송
        val potAfterJoin = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()
        if (potAfterJoin.status == PotStatus.SUCCESS) {
            val participantIds = participantRepository.findUserIdsByPotId(potId)
            pushService.sendNotificationToUsers(
                participantIds,
                "SNUXI 모집 완료",
                "참여하신 팟의 모집이 완료되었습니다. 채팅을 확인해주세요."
            )
        }
        // user active pot id 또한 업데이트
        updateActivePotIdUsers(listOf(userId), potId)

        // 챗봇 자동 메시지 전송
        val username = userRepository.findById(userId).orElseThrow {
            UserNotFoundException()
        }.username
        chatBotService.sendJoinMsg(potId, userId, username)
    }

    @Transactional
    fun leavePot(userId: Long, potId: Long) {
        // 존재하지 않는 팟, 또는 참여 정보가 일치하지 않으면 예외 던짐
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()
        if (!participantRepository.existsByUserIdAndPotId(userId, potId)) throw NotParticipatingException()

        // user active pot id & participant 정보 삭제
        updateActivePotIdUsers(listOf(userId), null)
        val deletedCount = participantRepository.deleteByUserIdAndPotIdReturnCount(userId, potId)
        if(deletedCount == 0) throw NotParticipatingException()

        // 원자적 update
        val updated = potRepository.tryLeavePot(
            potId = potId,
            recruitingStatus = PotStatus.RECRUITING
        )
        if (updated == 0) throw TemporarilyNotLeavePotException()

        // 모든 DB 작업 성공 후, 메시지 전송
        val username = userRepository.findById(userId).orElseThrow {
            UserNotFoundException()
        }.username
        chatBotService.sendLeaveMsg(potId, userId, username)

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
        departureId: Long?,
        destinationId: Long?,
        pageable: Pageable
    ): Page<PotDto> {
        val targetStatuses = listOf(PotStatus.RECRUITING, PotStatus.SUCCESS)
        val listPots = when {
            departureId == null && destinationId == null ->
                potRepository.findAvailableAll(targetStatuses, pageable)

            departureId == null && destinationId != null ->
                // findAvailableAll을 findAvailableByDestination으로 변경
                potRepository.findAvailableByDestination(destinationId, targetStatuses, pageable)

            departureId != null && destinationId == null ->
                potRepository.findAvailableByDeparture(departureId, targetStatuses, pageable)

            else ->
                potRepository.findAvailableWithDest(
                    departureId!!, destinationId!!, targetStatuses, pageable)
        }
        val ownerIds = listPots.content.map { it.ownerId }.distinct()
        val ownersMap = userRepository.findAllById(ownerIds).associateBy({ it.id!! }, { it.username })

        return listPots.map { pot ->
            val ownerName = ownersMap[pot.ownerId] ?: "알 수 없는 사용자"
            PotDto.from(pot, ownerName)
        }
    }

    @Transactional(readOnly = true)
    fun getMyPot(userId: Long): PotDto? {
        val participation = participantRepository.findByUserId(userId) ?: return null
        val pot = potRepository.findByIdOrNull(participation.potId) ?: return null
        val owner = userRepository.findByIdOrNull(pot.ownerId)
        val ownerName = owner ?.username ?: "알 수 없는 사용자"
        val unreadCount = chatMessageRepository.countByPotIdAndIdGreaterThan(
            pot.id!!,
            participation.lastReadMessageId
        )
        return PotDto.from(pot, ownerName, unreadCount)
    }

    private fun updateActivePotIdUsers(
        userIds: List<Long>,
        potId: Long?
    ) {
        userRepository.updateActivePotIdForUsers(userIds, potId)
    }

    @Transactional
    fun kickParticipant(requestUserId: Long, potId: Long, targetUserId: Long) {
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()

        if (pot.ownerId != requestUserId) throw NotPotOwnerException()
        if (requestUserId == targetUserId) throw CannotKickSelfException()
        if (!participantRepository.existsByUserIdAndPotId(targetUserId, potId)) throw NotParticipatingException()

        val targetUser = userRepository.findByIdOrNull(targetUserId) ?: throw UserNotFoundException()
        val targetUserName = targetUser.username

        updateActivePotIdUsers(listOf(targetUserId), null)

        // userId -> targetUserId로 수정, ANd -> And로 수정
        val deletedCount = participantRepository.deleteByUserIdAndPotIdReturnCount(targetUserId, potId)
        if(deletedCount == 0) throw NotParticipatingException()

        val updated = potRepository.tryLeavePot(
            potId = potId,
            recruitingStatus = PotStatus.RECRUITING
        )

        if (updated == 0) throw TemporarilyNotLeavePotException()

        // 강퇴당한 유저에게 알림 전송
        pushService.sendNotificationToUser(targetUserId, "SNUXI 강퇴 알림", "참여 중이던 팟에서 강퇴되었습니다.")
        chatBotService.sendKickMsg(potId, targetUserName)
    }

    @Transactional(readOnly = true)
    fun generateKakaoDeepLink(
        potId: Long,
        userId: Long
    ): String {
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()

        // 방장이 아니면 딥링크를 만들 수 없다
        if(pot.ownerId != userId) throw KakaoDeepLinkNotOwnerException()

        val start = landmarkRepository.findByIdOrNull(pot.departureId) ?: throw RegionNotFoundException()
        val end = landmarkRepository.findByIdOrNull(pot.destinationId) ?: throw RegionNotFoundException()

        return LandmarkDto.generateKakaoLink(
            origin = LandmarkDto.from(start),
            dest = LandmarkDto.from(end)
        )
    }

    @Transactional
    fun togglePotStatus(userId: Long, potId: Long): PotDto {
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()

        // 방장 확인
        if (pot.ownerId != userId) throw NotPotOwnerException()

        // True <-> False
        pot.isLocked = !pot.isLocked

        val ownerName = userRepository.findByIdOrNull(pot.ownerId)?.username ?: "알 수 없는 사용자"
        return PotDto.from(pot, ownerName)
    }

    @Transactional(readOnly = true)
    fun getPotParticipants(potId: Long): List<PotParticipantResponse> {
        // 팟이 존재하는 지 확인
        val pot = potRepository.findByIdOrNull(potId) ?: throw PotNotFoundException()

        // 유저 리스트 가져오기
        val users = participantRepository.findUsersByPotId(potId)

        return users.map { user ->
            val isOwner = (user.id == pot.ownerId)
            PotParticipantResponse.from(user, isOwner)
        }
    }
}