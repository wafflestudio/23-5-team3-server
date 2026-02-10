package com.snuxi.admin.dto

import com.snuxi.user.model.ReportReason
import java.time.LocalDateTime

data class AdminStatsResponse(
    val summary: StatsSummary,
    val analysis: StatsAnalysis
)

data class StatsSummary(
    val activeUsers: Long,        // 현재 서비스 중인 유저 수
    val cumulativeUsers: Long,    // 지금까지 거쳐간 총 유저 수 (MAX ID)
    val currentPots: Long,        // 현재 DB에 남아있는 팟 수
    val unprocessedReports: Long  // 아직 처리 안 된 신고 수
)

data class StatsAnalysis(
    val reportReasons: Map<ReportReason, Long>, // 신고 사유별 통계
    val suspendedUsers: List<String>            // 현재 정지 상태인 유저 목록
)