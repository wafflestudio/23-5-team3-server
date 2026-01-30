package com.snuxi.pot.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "landmarks")
class Landmark (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "landmark_name", nullable = false)
    val landmarkName: String,

    @Column(nullable = false, precision = 7, scale = 5)
    val latitude: BigDecimal,

    @Column(nullable = false, precision = 8, scale = 5)
    val longitude: BigDecimal
)