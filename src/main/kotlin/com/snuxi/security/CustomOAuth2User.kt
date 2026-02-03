package com.snuxi.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime

data class CustomOAuth2User(
    val userId: Long,
    val suspendedUntil: LocalDateTime? = null,
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: Map<String, Any>,
    private val nameAttributeKey: String
) : OAuth2User {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getAttributes(): Map<String, Any> = attributes
    override fun getName(): String = attributes[nameAttributeKey]?.toString() ?: userId.toString()
}
