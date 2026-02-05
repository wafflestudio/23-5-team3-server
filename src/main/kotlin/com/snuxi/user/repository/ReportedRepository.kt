package com.snuxi.user.repository

import com.snuxi.user.model.Reported
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ReportedRepository : JpaRepository<Reported, Long> {

    // 전체 조회 (최신순 정렬)
    fun findAllByOrderByReportedAtDesc(pageable: Pageable): Page<Reported>

    // 처리 여부로 필터링해서 조회 (최신순 정렬)
    fun findAllByIsProcessedOrderByReportedAtDesc(isProcessed: Boolean, pageable: Pageable): Page<Reported>

    // 특정 유저의 신고 내역 모아보기
    fun findAllByReporterUserIdOrderByReportedAtDesc(reporterUserId: Long, pageable: Pageable): Page<Reported>

    // 특정 유저의 신고당한 내역 모아보기
    fun findAllByReportedUserIdOrderByReportedAtDesc(reportedUserId: Long, pageable: Pageable): Page<Reported>

    fun countByIsProcessed(isProcessed: Boolean): Long

    @Query("SELECT r.reason, COUNT(r) FROM Reported r GROUP BY r.reason")
    fun countReportsByReason(): List<Array<Any>>
}