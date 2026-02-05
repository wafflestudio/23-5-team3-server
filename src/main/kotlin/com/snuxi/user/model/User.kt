package com.snuxi.user.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime


@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var username: String,

    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    val activePotId: Long? = null,

    @Column(name = "notification_enabled")
    var notificationEnabled: Boolean = true,

    var suspendedUntil: LocalDateTime? = null,

    @Column(name = "suspension_count")
    var suspensionCount: Int = 0
) {
    @CreatedDate
    var createdAt: Instant? = null

    @LastModifiedDate
    var updatedAt: Instant? = null
}

enum class Role {
    USER, ADMIN, CHATBOT
}

