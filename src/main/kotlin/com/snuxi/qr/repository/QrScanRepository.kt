package com.snuxi.qr.repository

import com.snuxi.qr.entity.QrScan
import org.springframework.data.jpa.repository.JpaRepository

interface QrScanRepository : JpaRepository<QrScan, Long>
