package com.snuxi.terms.repository

import com.snuxi.terms.entity.UserTermsAgreement
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal

interface UserTermsAgreementRepository : JpaRepository<UserTermsAgreement, Long> {
    fun existsByUserId(
        userId: Long
    ): Boolean

    fun existsByUserIdAndTermsVersion(
        userId: Long,
        termsVersion: BigDecimal
    ): Boolean
}