package com.snuxi.user.service

import com.snuxi.user.UserNotFoundException
import com.snuxi.user.dto.UserResponse
import com.snuxi.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    val userRepository: UserRepository
) {
    fun getProfile(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        return UserResponse(user)
    }
    fun updateUsername(email: String, newName: String): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        user.username = newName
        return UserResponse(user)
    }

    @Transactional
    fun updateNotificationEnabled(userId: Long, enabled: Boolean) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        user.notificationEnabled = enabled
    }

    @Transactional
    fun updateNotificationEnabled(userId: Long, enabled: Boolean) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        user.notificationEnabled = enabled
    }
}