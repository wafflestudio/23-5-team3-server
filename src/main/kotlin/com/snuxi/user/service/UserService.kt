package com.snuxi.user.service

import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.user.UserNotFoundException
import com.snuxi.user.dto.UserResponse
import com.snuxi.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.notification.repository.UserDeviceRepository
import com.snuxi.pot.repository.PotRepository
import com.snuxi.pot.service.PotService
import com.snuxi.terms.repository.UserTermsAgreementRepository

@Service
@Transactional
class UserService(
    val userRepository: UserRepository,
    private val participantRepository: ParticipantRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val potRepository: PotRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val potService: PotService,
    private val userTermsAgreementRepository: UserTermsAgreementRepository
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
        participantRepository.findByUserId(userId)?.let { participation ->
            potService.leavePot(userId, participation.potId)
        }
        chatMessageRepository.anonymizeSender(userId, 0L)
        userTermsAgreementRepository.deleteAllByUserId(userId)
        // 등록된 기기 정보 삭제(알림)
        userDeviceRepository.deleteAllByUserId(userId)
        // 유저 삭제
        userRepository.delete(user)
    }
}