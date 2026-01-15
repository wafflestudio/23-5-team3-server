package com.snuxi.pot.repository

import com.snuxi.pot.entity.Pots
import org.springframework.data.jpa.repository.JpaRepository

interface PotRepository : JpaRepository<Pots, Long> {

}