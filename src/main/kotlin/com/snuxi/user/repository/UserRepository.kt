package com.snuxi.user.repository

import com.snuxi.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.time.LocalDateTime

interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.activePotId = :potId where u.id in :userIds")
    fun updateActivePotIdForUsers(
        userIds: List<Long>,
        potId: Long?
    ): Int
    fun countByCreatedAtBetween(start: Instant, end: Instant): Long
    fun findBySuspendedUntilAfter(now: LocalDateTime): List<User>
    // 누적 유저 수 측정용 max id 조회
    @Query("SELECT MAX(u.id) FROM User u")
    fun findMaxId(): Long?
}