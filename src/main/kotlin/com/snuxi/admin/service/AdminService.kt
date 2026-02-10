package com.snuxi.admin.service

import com.snuxi.admin.dto.*
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.pot.PotStatus
import com.snuxi.pot.repository.PotRepository
import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.repository.UserRepository
import com.snuxi.user.UserNotFoundException
import com.snuxi.user.repository.ReportedRepository
import com.snuxi.pot.service.PotService
import com.snuxi.participant.repository.ParticipantRepository
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.session.SessionRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val sessionRegistry: SessionRegistry,
    private val potRepository: PotRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val reportedRepository: ReportedRepository,
    private val potService: PotService,
    private val participantRepository: ParticipantRepository
) {

    @Transactional
    fun suspendUser(targetUserId: Long, days: Long) {
        // 유저 조회
        val user = userRepository.findById(targetUserId)
            .orElseThrow { UserNotFoundException() }

        // DB 업데이트 (정지 기간 & 횟수)
        user.suspendedUntil = LocalDateTime.now().plusDays(days)
        user.suspensionCount += 1
        
        //leavePot 실행. 정지 시 나가지도록 처리
        participantRepository.findByUserId(targetUserId)?.let { participation ->
            potService.leavePot(targetUserId, participation.potId)
        }

        // 모든 접속자 명단 가져오기
        val principals = sessionRegistry.allPrincipals

        for (principal in principals) {
            if (principal is CustomOAuth2User && principal.userId == targetUserId) {
                // 이 유저의 모든 세션 가져오기
                val sessions = sessionRegistry.getAllSessions(principal, false)

                // 세션 폭파 (다음 요청 시 로그아웃됨)
                for (session in sessions) {
                    session.expireNow()
                }
            }
        }
    }

    @Transactional(readOnly = true)
    fun getFullStatistics(): AdminStatsResponse {
        val now = LocalDateTime.now()

        // 1. 요약 데이터 (MAX ID 꼼수 포함)
        val summary = StatsSummary(
            activeUsers = userRepository.count(),
            cumulativeUsers = userRepository.findMaxId() ?: 0L,
            currentPots = potRepository.count(),
            unprocessedReports = reportedRepository.countByIsProcessed(false)
        )

        // 2. 신고 사유 통계
        val reportCounts = reportedRepository.countReportsByReason().associate {
            it[0] as com.snuxi.user.model.ReportReason to it[1] as Long
        }

        // 3. 분석 데이터 (정지 유저)
        val analysis = StatsAnalysis(
            reportReasons = reportCounts,
            suspendedUsers = userRepository.findBySuspendedUntilAfter(now).map { it.username }
        )

        return AdminStatsResponse(summary, analysis)
    }
}