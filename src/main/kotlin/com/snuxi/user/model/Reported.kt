package com.snuxi.user.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reported")
class Reported(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "is_processed", nullable = false)
    var isProcessed: Boolean = false,

    @Column(name = "reported_at", nullable = false)
    val reportedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "reporter_user_id", nullable = false)
    val reporterUserId: Long,

    @Column(name = "reporter_email", nullable = false)
    val reporterEmail: String,

    @Column(name = "reported_user_id", nullable = false)
    val reportedUserId: Long,

    @Column(name = "reported_email", nullable = false)
    val reportedEmail: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 16)
    val reason: ReportReason,

    @Column(name = "messages", nullable = false, columnDefinition = "LONGTEXT")
    val messages: String
)