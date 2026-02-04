package com.snuxi.admin.dto

import com.snuxi.user.model.ReportReason
import java.time.LocalDateTime

data class AdminStatsResponse(
    val summary: StatsSummary,
    val dailyTrends: List<DailyTrendDto>,
    val hourlyActivity: List<HourlyActivityDto>,
    val analysis: StatsAnalysis
)

data class StatsSummary(
    val totalUsers: Long,
    val totalSuccessPots: Long,
    val totalMessages: Long,
    val unprocessedReports: Long
)

data class DailyTrendDto(
    val date: String,
    val newUsers: Long,
    val activeUsers: Long,
    val createdPots: Long,
    val messages: Long
)

data class HourlyActivityDto(val hour: Int, val chatCount: Long, val potCount: Long)

data class StatsAnalysis(
    val successRate: Double,
    val topRoutes: List<RouteRankDto>,
    val reportReasons: Map<ReportReason, Long>,
    val suspendedUsers: List<String>
)

data class RouteRankDto(val routeName: String, val count: Long)