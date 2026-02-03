package com.snuxi.user.service

import com.snuxi.user.ReportNotFoundException
import com.snuxi.user.dto.ReportDetailResponse
import com.snuxi.user.dto.ReportSummaryResponse
import com.snuxi.user.repository.ReportedRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminReportService(
    private val reportedRepository: ReportedRepository
) {
    @Transactional(readOnly = true)
    fun getReports(
        isProcessed: Boolean?,
        reporterId: Long?,
        reportedId: Long?,
        pageable: Pageable
    ): Page<ReportSummaryResponse> {
        val reports = when {
            isProcessed != null -> reportedRepository.findAllByIsProcessedOrderByReportedAtDesc(isProcessed, pageable)
            reporterId != null -> reportedRepository.findAllByReporterUserIdOrderByReportedAtDesc(reporterId, pageable)
            reportedId != null -> reportedRepository.findAllByReportedUserIdOrderByReportedAtDesc(reportedId, pageable)
            else -> reportedRepository.findAllByOrderByReportedAtDesc(pageable)
        }
        return reports.map { ReportSummaryResponse.from(it) }
    }

    private val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        .findAndRegisterModules()
    @Transactional(readOnly = true)
    fun getReportDetail(reportId: Long): ReportDetailResponse {
        val report = reportedRepository.findById(reportId)
            .orElseThrow { ReportNotFoundException() }


        val chatLogs: List<com.snuxi.user.dto.ChatLogDto> = try {
            objectMapper.readValue(
                report.messages,
                object : com.fasterxml.jackson.core.type.TypeReference<List<com.snuxi.user.dto.ChatLogDto>>() {}
            )
        } catch (e: Exception) {
            emptyList()
        }


        return ReportDetailResponse.from(report, chatLogs)
    }

    @Transactional
    fun toggleReportStatus(reportId: Long): Boolean {
        val report = reportedRepository.findById(reportId)
            .orElseThrow { ReportNotFoundException() }
        report.isProcessed = !report.isProcessed
        return report.isProcessed
    }
}