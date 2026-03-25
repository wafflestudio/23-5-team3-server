package com.snuxi.user.repository

import com.snuxi.user.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.LocalDateTime

interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun countByCreatedAtBetween(start: Instant, end: Instant): Long
    fun findBySuspendedUntilAfter(now: LocalDateTime): List<User>

    @Query(
        """
        SELECT u
        FROM User u
        WHERE (
            :q IS NULL
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND (
            :status = 'all'
            OR (:status = 'active' AND (u.suspendedUntil IS NULL OR u.suspendedUntil <= :now))
            OR (:status = 'suspended' AND u.suspendedUntil > :now)
        )
        ORDER BY u.createdAt DESC
        """
    )
    fun searchAdminUsers(
        @Param("q") q: String?,
        @Param("status") status: String,
        @Param("now") now: LocalDateTime,
        pageable: Pageable
    ): Page<User>

    // 누적 유저 수 측정용 max id 조회
    @Query("SELECT MAX(u.id) FROM User u")
    fun findMaxId(): Long?
}
