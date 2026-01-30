package com.snuxi.notification.service

import com.snuxi.infra.fcm.FcmPushClient
import com.snuxi.notification.repository.UserDeviceRepository
import com.snuxi.user.repository.UserRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushService(
    private val userRepository: UserRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val fcmPushClient: FcmPushClient
) {
    @Async
    @Transactional(readOnly = true) // 1:1 용도 (계정 정지 알람 등)
    fun sendNotificationToUser(userId: Long, title: String, body: String) {
        val user = userRepository.findById(userId).orElse(null) ?: return

        if (!user.notificationEnabled) return

        val tokens = userDeviceRepository.findAllByUserId(userId).map { it.fcmToken }

        fcmPushClient.sendMessages(tokens, title, body)
    }

    @Async
    @Transactional(readOnly = true) // 다대다 용도 (채팅방 단체알림)
    fun sendNotificationToUsers(userIds: List<Long>, title: String, body: String) {
        val enabledUserIds = userRepository.findAllById(userIds)
            .filter { it.notificationEnabled }
            .map { it.id!! }

        if (enabledUserIds.isEmpty()) return

        val tokens = userDeviceRepository.findAllByUserIdIn(enabledUserIds).map { it.fcmToken }
        fcmPushClient.sendMessages(tokens, title, body)
    }
}