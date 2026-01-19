package com.snuxi.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class CurrentUserIdResolver (

) {
    fun getCurrentUserId(): Long {
        val auth = SecurityContextHolder.getContext().authentication ?: throw IllegalStateException("Authentication info not found")

        val principal = auth.principal
        return when (principal){
            is CustomOAuth2User -> principal.userId
            else -> throw IllegalStateException("User info not found")
        }
    }
}