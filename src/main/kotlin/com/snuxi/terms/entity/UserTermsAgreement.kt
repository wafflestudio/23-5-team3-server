package com.snuxi.terms.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "user_terms_agreements")
class UserTermsAgreement (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "terms_version")
    val termsVersion: BigDecimal,
    @Column(name = "agree_at")
    val agreeAt: LocalDateTime,
    @Column(name = "ip")
    val ip: String? = null,
    @Column(name = "user_device")
    val userDevice: String? = null
)