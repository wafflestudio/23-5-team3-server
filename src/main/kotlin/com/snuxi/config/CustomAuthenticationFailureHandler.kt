package com.snuxi.config

import com.snuxi.user.NotSnuMailException
import com.snuxi.user.SuspendedUserException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Component
class CustomAuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        // 0. 실패한 OAuth 쿠키 정리 (재시도 시 이전 계정 자동 선택 방지)
        CookieUtils.deleteCookie(request, response,
            HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        CookieUtils.deleteCookie(request, response,
            HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)

        // 1. 예외 타입에 따라 에러 메시지 결정
        val errorMessage = when (exception) {
            is NotSnuMailException -> exception.message ?: "서울대학교 계정만 사용 가능합니다."
            is SuspendedUserException -> exception.message ?: "정지된 계정입니다."
            else -> "로그인에 실패했습니다. 관리자에게 문의하세요. (${exception.message})"
        }

        // 2. 한글 메시지 인코딩 (URL 파라미터 깨짐 방지)
        val encodedMsg = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)

        // 3. 리다이렉트 URL 생성 (메인페이지 '/' 로 이동하면서 파라미터 전달)
        // 도메인 변경 이슈로 절대 경로로 수정
        // 예: http://localhost:8080/?error=true&message=%EC%A0%95...
        val targetUrl = UriComponentsBuilder.fromUriString("https://snuxi.com/error")
            .queryParam("message", encodedMsg)
            .build()
            .toUriString()

        // 4. 리다이렉트 수행
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}