package com.snuxi.notification.service

import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.pot.PotStatus
import com.snuxi.pot.repository.PotRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class PotNotificationScheduler(
    private val potRepository: PotRepository,
    private val participantRepository: ParticipantRepository,
    private val pushService: PushService
) {
    // 매 분 0초마다 스케쥴러 실행
    @Scheduled(cron = "0 * * * * *")
    @Transactional(readOnly = true)
    fun sendDepartureReminder() {
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        val targetTimeStart = now.plusMinutes(15)
        val targetTimeEnd = targetTimeStart.plusSeconds(59)

        // 15분 뒤에 출발하는 팟들 조회하기
        val upcomingPots = potRepository.findAllByDepartureTimeBetweenAndStatusIn(
            targetTimeStart,
            targetTimeEnd,
            listOf(PotStatus.RECRUITING, PotStatus.SUCCESS)
        )

        upcomingPots.forEach { pot ->
            val participantIds = participantRepository.findUserIdsByPotId(pot.id!!)
            if (participantIds.isNotEmpty()) {
                pushService.sendNotificationToUsers(
                    participantIds,
                    "출발 15분 전 리마인더",
                    "15분 뒤에 택시팟이 출발 예정입니다. 출발 장소로 향해 주세요."
                )
            }
        }
    }
}