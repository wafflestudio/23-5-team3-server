package com.snuxi.qr.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "qr_scans")
class QrScan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 32)
    val code: String,

    @Column(nullable = false)
    val scannedAt: Instant = Instant.now(),

    @Column(length = 512)
    val userAgent: String? = null,

    @Column(length = 45)
    val ip: String? = null,

    @Column(length = 512)
    val referer: String? = null,
)
