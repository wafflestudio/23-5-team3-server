package com.snuxi.user.service

import com.snuxi.user.UserNotFoundException
import com.snuxi.user.dto.UserResponse
import com.snuxi.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    val userRepository: UserRepository
) {
    fun getProfile(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        return UserResponse(user)
    }
}