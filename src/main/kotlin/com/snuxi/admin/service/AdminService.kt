package com.snuxi.admin.service

import com.snuxi.admin.InvalidAdminFilterException
import com.snuxi.admin.dto.AdminPotListItem
import com.snuxi.admin.dto.AdminPotListResponse
import com.snuxi.admin.dto.AdminStatsResponse
import com.snuxi.admin.dto.AdminUserListItem
import com.snuxi.admin.dto.AdminUserListResponse
import com.snuxi.admin.dto.StatsAnalysis
import com.snuxi.admin.dto.StatsSummary
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.pot.repository.LandmarkRepository
import com.snuxi.pot.repository.PotRepository
import com.snuxi.pot.service.PotService
import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.UserNotFoundException
import com.snuxi.user.repository.ReportedRepository
import com.snuxi.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.session.SessionRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val sessionRegistry: SessionRegistry,
    private val potRepository: PotRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val reportedRepository: ReportedRepository,
    private val potService: PotService,
    private val participantRepository: ParticipantRepository,
    private val landmarkRepository: LandmarkRepository
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
        participantRepository.findAllByUserId(targetUserId).forEach { participation ->
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

    @Transactional
    fun unsuspendUser(targetUserId: Long) {
        val user = userRepository.findById(targetUserId)
            .orElseThrow { UserNotFoundException() }

        user.suspendedUntil = null
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

    @Transactional(readOnly = true)
    fun getUsers(page: Int, size: Int, q: String?, status: String): AdminUserListResponse {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 100)
        val normalizedStatus = status.lowercase()
        if (normalizedStatus !in setOf("all", "active", "suspended")) {
            throw InvalidAdminFilterException("status must be one of: all, active, suspended")
        }

        val pageable = PageRequest.of(safePage, safeSize)
        val now = LocalDateTime.now()
        val userPage = userRepository.searchAdminUsers(
            q = q?.trim()?.takeIf { it.isNotBlank() },
            status = normalizedStatus,
            now = now,
            pageable = pageable
        )

        return AdminUserListResponse(
            content = userPage.content.map { user ->
                AdminUserListItem(
                    id = user.id!!,
                    email = user.email,
                    username = user.username,
                    role = user.role.name,
                    createdAt = user.createdAt,
                    suspended = user.suspendedUntil?.isAfter(now) ?: false
                )
            },
            page = userPage.number,
            size = userPage.size,
            totalElements = userPage.totalElements,
            totalPages = userPage.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun getPots(page: Int, size: Int, status: String): AdminPotListResponse {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 100)
        val normalizedStatus = status.lowercase()
        if (normalizedStatus !in setOf("all", "open", "closed")) {
            throw InvalidAdminFilterException("status must be one of: all, open, closed")
        }

        val pageable = PageRequest.of(safePage, safeSize)
        val potPage = potRepository.searchAdminPots(normalizedStatus, pageable)

        val landmarkIds = potPage.content.flatMap { listOf(it.departureId, it.destinationId) }.distinct()
        val landmarksMap = landmarkRepository.findAllById(landmarkIds).associateBy({ it.id!! }, { it.landmarkName })

        return AdminPotListResponse(
            content = potPage.content.map { pot ->
                AdminPotListItem(
                    potId = pot.id!!,
                    departureName = landmarksMap[pot.departureId] ?: "알 수 없음",
                    destinationName = landmarksMap[pot.destinationId] ?: "알 수 없음",
                    departureTime = pot.departureTime,
                    participantCount = pot.currentCount,
                    kakaoDeepLinkStatus = pot.kakaoCallStatus.name,
                    kakaoDeepLinkAt = pot.kakaoCallAt,
                    kakaoDeepLinkError = pot.kakaoCallError,
                    createdAt = pot.createdAt
                )
            },
            page = potPage.number,
            size = potPage.size,
            totalElements = potPage.totalElements,
            totalPages = potPage.totalPages
        )
    }
}