package com.snuxi.notification.service

import com.snuxi.infra.fcm.FcmPushClient
import com.snuxi.notification.repository.UserDeviceRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class PushService(
    private val userDeviceRepository: UserDeviceRepository,
    private val fcmPushClient: FcmPushClient
) {
    @Async
    fun sendChatNotification(receiverIds: List<Long>, senderName: String, message: String) {
        // SNUTT 방식: 유저 ID 목록으로 기기 토큰 긁어오기
        val tokens = userDeviceRepository.findAllByUserIdIn(receiverIds).map { it.fcmToken }
        fcmPushClient.sendMessages(tokens, senderName, message)
    }
}