package com.snuxi.user.repository

import com.snuxi.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.activePotId = :potId where u.id in :userIds")
    fun updateActivePotIdForUsers(
        userIds: List<Long>,
        potId: Long?
    ): Int

}