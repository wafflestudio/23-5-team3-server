package com.snuxi.user.repository

import com.snuxi.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findByUserEmail(userEmail: String): User?
}