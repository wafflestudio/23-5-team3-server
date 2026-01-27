package com.snuxi.pot.repository

import com.snuxi.pot.model.Landmark
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LandmarkRepository : JpaRepository<Landmark, Long>