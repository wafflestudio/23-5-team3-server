package com.snuxi.participant.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "participants")
class Participants (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "user_id") var userId: Long,
    @Column(name = "pot_id") var potId: Long,
    @Column(name = "joined_at") var joinedAt: LocalDateTime,
    @Column(name = "last_read_message_id") var lastReadMessageId: Long = 0
)