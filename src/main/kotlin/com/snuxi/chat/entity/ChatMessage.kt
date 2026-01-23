package com.snuxi.chat.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_message")
class ChatMessage (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "pot_id") var potId: Long,
    @Column(name = "sender_id") var senderId: Long,
    @Column(name = "text") var text: String,
    @Column(name = "datetime_send_at") var datetimeSendAt: LocalDateTime
)