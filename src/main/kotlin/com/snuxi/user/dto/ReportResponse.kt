package com.snuxi.user.dto

import com.snuxi.user.model.ReportReason
import com.snuxi.user.model.Reported
import java.time.LocalDateTime

// ChatMessageItemDto와 필드명 통일해서 구현
data class ChatLogDto(
    val id: Long,
    val senderId: Long,
    val username: String,
    val text: String,
    val datetimeSendAt: LocalDateTime
)

//  목록 조회용
data class ReportSummaryResponse(
    val id: Long,
    val isProcessed: Boolean,
    val reportedAt: LocalDateTime,
    val reporterUserId: Long,
    val reporterEmail: String,
    val reportedUserId: Long,
    val reportedEmail: String,
    val reason: ReportReason
) {
    companion object {
        fun from(entity: Reported) = ReportSummaryResponse(
            id = entity.id!!,
            isProcessed = entity.isProcessed,
            reportedAt = entity.reportedAt,
            reporterUserId = entity.reporterUserId,
            reporterEmail = entity.reporterEmail,
            reportedUserId = entity.reportedUserId,
            reportedEmail = entity.reportedEmail,
            reason = entity.reason
        )
    }
}

// 상세 조회용
data class ReportDetailResponse(
    val id: Long,
    val isProcessed: Boolean,
    val reportedAt: LocalDateTime,
    val reporterUserId: Long,
    val reporterEmail: String,
    val reportedUserId: Long,
    val reportedEmail: String,
    val reason: ReportReason,
    val chatLogs: List<ChatLogDto>
) {
    companion object {
        fun from(entity: Reported, chatLogs: List<ChatLogDto>) = ReportDetailResponse(
            id = entity.id!!,
            isProcessed = entity.isProcessed,
            reportedAt = entity.reportedAt,
            reporterUserId = entity.reporterUserId,
            reporterEmail = entity.reporterEmail,
            reportedUserId = entity.reportedUserId,
            reportedEmail = entity.reportedEmail,
            reason = entity.reason,
            chatLogs = chatLogs
        )
    }
}