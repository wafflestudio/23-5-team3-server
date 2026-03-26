package com.snuxi.pot.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "landmark_pair_fares")
@IdClass(LandmarkPairFareId::class)
class LandmarkPairFare(
    @Id
    @Column(name = "departure_id")
    val departureId: Long,

    @Id
    @Column(name = "destination_id")
    val destinationId: Long,

    @Column(name = "estimated_fare", nullable = false)
    val estimatedFare: Int,

    @Column(name = "distance_m")
    val distanceM: Int?,

    @Column(name = "duration_s")
    val durationS: Int?,

    @Column(name = "source", nullable = false)
    val source: String,

    @Column(name = "collected_at", nullable = false)
    val collectedAt: LocalDateTime
)

class LandmarkPairFareId(
    var departureId: Long = 0,
    var destinationId: Long = 0
) : Serializable