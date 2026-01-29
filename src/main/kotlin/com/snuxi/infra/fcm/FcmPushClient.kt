package com.snuxi.infra.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Component

@Component
class FcmPushClient {
    // 벌크 전송(최대 500개) 최적화 로직 (snutt 참고)
    // 참고 링크: https://github.com/wafflestudio/snutt/blob/develop/core/src/main/kotlin/common/push/fcm/FcmPushClient.kt
    fun sendMessages(tokens: List<String>, title: String, body: String) {
        if (tokens.isEmpty()) return

        tokens.chunked(500).forEach { chunk ->
            val notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build()

            val message = MulticastMessage.builder()
                .addAllTokens(chunk)
                .setNotification(notification)
                .build()

            // 비동기로 전송 - 서버 저하 방지
            FirebaseMessaging.getInstance().sendEachForMulticastAsync(message)
        }
    }
}