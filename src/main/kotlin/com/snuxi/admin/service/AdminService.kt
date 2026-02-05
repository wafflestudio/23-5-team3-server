package com.snuxi.admin.service

import com.snuxi.admin.dto.*
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.pot.PotStatus
import com.snuxi.pot.repository.PotRepository
import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.repository.UserRepository
import com.snuxi.user.UserNotFoundException
import com.snuxi.user.repository.ReportedRepository
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
    private val reportedRepository: ReportedRepository
) {

    @Transactional
    fun suspendUser(targetUserId: Long, days: Long) {
        // 유저 조회
        val user = userRepository.findById(targetUserId)
            .orElseThrow { UserNotFoundException() }

        // DB 업데이트 (정지 기간 & 횟수)
        user.suspendedUntil = LocalDateTime.now().plusDays(days)
        user.suspensionCount += 1

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
        val sevenDaysAgo = now.minusDays(7).toLocalDate().atStartOfDay()

        val summary = StatsSummary(
            totalUsers = userRepository.count(),
            totalSuccessPots = potRepository.countByStatus(PotStatus.SUCCESS),
            totalMessages = chatMessageRepository.count(),
            unprocessedReports = reportedRepository.countByIsProcessed(false)
        )

        val dailyTrends = (0..6).reversed().map { i ->
            val date = now.minusDays(i.toLong()).toLocalDate()
            val start = date.atStartOfDay().toInstant(ZoneOffset.UTC)
            val end = date.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)

            DailyTrendDto(
                date = date.toString(),
                newUsers = userRepository.countByCreatedAtBetween(start, end),
                createdPots = potRepository.countByCreatedAtBetween(start, end),
                activeUsers = chatMessageRepository.countActiveUsersBetween(date.atStartOfDay(), date.atTime(23, 59, 59)),
                messages = chatMessageRepository.countByDatetimeSendAtBetween(date.atStartOfDay(), date.atTime(23, 59, 59))
            )
        }

        val chatHourMap = chatMessageRepository.countMessagesGroupedByHour().associate { it[0] as Int to it[1] as Long }
        val hourlyActivity = (0..23).map { h ->
            HourlyActivityDto(h, chatHourMap[h] ?: 0L, 0L) // 팟 생성 시간대도 동일 방식으로 추가 가능
        }

        val topRoutes = potRepository.findTopRoutes(PageRequest.of(0, 5)).map {
            RouteRankDto("${it[0]} → ${it[1]}", it[2] as Long)
        }

        val reportCounts = reportedRepository.countReportsByReason().associate {
            it[0] as com.snuxi.user.model.ReportReason to it[1] as Long
        }

        return AdminStatsResponse(
            summary = summary,
            dailyTrends = dailyTrends,
            hourlyActivity = hourlyActivity,
            analysis = StatsAnalysis(
                successRate = if(potRepository.count() > 0) (summary.totalSuccessPots.toDouble() / potRepository.count() * 100) else 0.0,
                topRoutes = topRoutes,
                reportReasons = reportCounts,
                suspendedUsers = userRepository.findBySuspendedUntilAfter(now).map { it.username }
            )
        )
    }
}