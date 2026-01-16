package com.snuxi.pot.entity

import com.snuxi.pot.PotStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import java.time.LocalDateTime

@Entity
@Table(name = "pots")
class Pots (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "owner_id") var ownerId: Long,
    @Column(name = "departure_id") var departureId: Long,
    @Column(name = "destination_id") var destinationId: Long,
    @Column(name = "departure_time") var departureTime: LocalDateTime,
    @Column(name = "min_capacity") var minCapacity: Int,
    @Column(name = "max_capacity") var maxCapacity: Int,
    @Column(name = "current_count") var currentCount: Int,
    @Column(name = "estimated_fee") var estimatedFee: Int,
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: PotStatus,

)