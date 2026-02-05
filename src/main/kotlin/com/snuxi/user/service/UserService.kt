package com.snuxi.user.service

import com.snuxi.user.UserNotFoundException
import com.snuxi.user.dto.UserResponse
import com.snuxi.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.notification.repository.UserDeviceRepository
import com.snuxi.pot.repository.PotRepository

@Service
@Transactional
class UserService(
    val userRepository: UserRepository,
    private val participantRepository: ParticipantRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val potRepository: PotRepository
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
    fun withdraw(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        // 참여 중인 팟 있다면 탈퇴
        potRepository.deleteAllByOwnerId(userId)
        participantRepository.deleteAllByUserId(userId)
        // 등록된 기기 정보 삭제(알림)
        userDeviceRepository.deleteAllByUserId(userId)
        // 유저 삭제
        userRepository.delete(user)
    }
}