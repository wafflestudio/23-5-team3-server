package com.snuxi.user.controller

import com.snuxi.user.dto.ReportDetailResponse
import com.snuxi.user.dto.ReportSummaryResponse

import com.snuxi.user.service.AdminReportService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/reports")
class AdminReportController(
    private val adminReportService: AdminReportService
) {
    @GetMapping
    fun getReports(
        @RequestParam(required = false) isProcessed: Boolean?,
        @RequestParam(required = false) reporterId: Long?,
        @RequestParam(required = false) reportedId: Long?,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<ReportSummaryResponse>> {
        return ResponseEntity.ok(adminReportService.getReports(isProcessed, reporterId, reportedId, pageable))
    }

    @GetMapping("/{reportId}")
    fun getReportDetail(@PathVariable reportId: Long): ResponseEntity<ReportDetailResponse> {
        return ResponseEntity.ok(adminReportService.getReportDetail(reportId))
    }

    @PatchMapping("/{reportId}/status")
    fun toggleStatus(@PathVariable reportId: Long): ResponseEntity<Boolean> {
        return ResponseEntity.ok(adminReportService.toggleReportStatus(reportId))
    }
}