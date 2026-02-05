package com.snuxi.terms.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class UserTermsAgreementService (
    @Value("\${app.terms-jwt.secret}")
    private val secret: String,
    @Value("\${app.terms-jwt.ttl-minutes}")
    private val ttlMinutes: Long,
    @Value("\${app.terms-jwt.issuer}")
    private val issuer: String
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    // 인증 후 일시적 JWT 토큰 발급
    fun issueToken(
        payload: TermsJwtPayload
    ): String {
        val now = Instant.now()
        val expire = now.plusSeconds(ttlMinutes * 60)

        return Jwts.builder().issuer(issuer)
            .subject(payload.email)
            .claim("purpose", "TERMS_AGREEMENT")
            .claim("name", payload.name)
            .claim("picture", payload.picture)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expire))
            .signWith(key)
            .compact()
    }

    // 약관 동의 후, 토큰 유효성 검증 후 세션에 주입
    fun verify(
        token: String
    ): TermsJwtPayload? {
        return try {
            // JWT 토큰 읽는 파서 생성
            val jwtParser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)

            // 토큰의 목적 추출
            val claims = jwtParser.payload
            val purpose = claims["purpose"] as? String
            if(purpose != "TERMS_AGREEMENT") return null

            TermsJwtPayload(
                email = claims.subject,
                name = claims["name"] as? String,
                picture = claims["picture"] as? String
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class TermsJwtPayload(
    val email: String,
    val name: String?,
    val picture: String?
)