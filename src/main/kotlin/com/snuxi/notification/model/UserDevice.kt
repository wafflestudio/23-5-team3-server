package com.snuxi.notification.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_devices")
class UserDevice(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "fcm_token")
    var fcmToken: String,

    @Column(name = "browser_type")
    var browserType: String? = null,

    @Column(name = "device_id")
    var deviceId: String,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)