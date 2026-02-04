package com.snuxi.user.controller

import com.snuxi.security.CurrentUserIdResolver
import com.snuxi.user.dto.ReportMessageRequestDto
import com.snuxi.user.dto.ReportMessageResponseDto
import com.snuxi.user.service.UserReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rooms/{roomId}/reports")
class UserReportController(
    private val currentUserIdResolver: CurrentUserIdResolver,
    private val userReportService: UserReportService
) {
    @PostMapping
    fun reportMessage(
        @PathVariable("roomId") roomId: Long,
        @RequestBody reportMessageRequestDto: ReportMessageRequestDto
    ): ResponseEntity<ReportMessageResponseDto> {
        val currentUserId = currentUserIdResolver.getCurrentUserId()

        val reason = reportMessageRequestDto.reason
        val targetMessageId = reportMessageRequestDto.targetMessageId
        val reportedUserId = reportMessageRequestDto.reportedUserId

        val reportId = userReportService.reportMessage(roomId, currentUserId, reason, targetMessageId, reportedUserId)

        val reportMessageResponseDto = ReportMessageResponseDto(
            reportId = reportId
        )

        return ResponseEntity.ok(reportMessageResponseDto)
    }
}