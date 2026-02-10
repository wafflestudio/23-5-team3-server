package com.snuxi.pot.service

import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.pot.PotStatus
import com.snuxi.pot.repository.PotRepository
import com.snuxi.user.model.User
import com.snuxi.user.repository.UserRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PotCleanupScheduler (
    private val potRepository: PotRepository,
    private val participantRepository: ParticipantRepository,
    private val userRepository: UserRepository
){
    // 10분마다 출발 시간 지난 팟 삭제
    @Scheduled(fixedDelay = 600000)
    @Transactional
    fun cleanupExpiredPots() {
        val now = LocalDateTime.now()
        // 출발 시간 지난 RECRUITING 상태
        val failedPots = potRepository.findAllByDepartureTimeBeforeAndStatusIn(
            now, listOf(PotStatus.RECRUITING)
        )

        // 출발한 지 2시간이 지난 SUCCESS 상태 (정산 시간 2시간 보장)
        val settledPots = potRepository.findAllByDepartureTimeBeforeAndStatusIn(
            now.minusHours(2), listOf(PotStatus.SUCCESS)
        )

        val expiredPots = failedPots + settledPots
        if (expiredPots.isNotEmpty()){
            val expiredPotIds = expiredPots.mapNotNull { it.id }

            //참여중인 유저들은 해제
            val targetUserIds = participantRepository.findUserIdsByPotIds(expiredPotIds)
            if (targetUserIds.isNotEmpty()){
                userRepository.updateActivePotIdForUsers(targetUserIds, null)
            }

            participantRepository.deleteAllByPotIdIn(expiredPotIds)

            //상태 EXPIRED로 변경
            potRepository.updateStatusToExpiredByIds(expiredPotIds)
        }
    }
}