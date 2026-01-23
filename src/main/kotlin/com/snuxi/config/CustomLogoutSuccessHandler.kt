package com.snuxi.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomLogoutSuccessHandler : SimpleUrlLogoutSuccessHandler() {

    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        // 'redirect_uri' 파라미터 확인
        val redirectUri = request.getParameter("redirect_uri")

        val targetUrl = if (!redirectUri.isNullOrBlank()) redirectUri else "/"

        // redirect
        if (response.isCommitted) return
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}