package com.snuxi.config

import com.snuxi.global.util.CookieUtils
import com.snuxi.security.CustomOAuth2User
import com.snuxi.terms.repository.UserTermsAgreementRepository
import com.snuxi.terms.service.TermsJwtPayload
import com.snuxi.terms.service.UserTermsAgreementService
import com.snuxi.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder

@Component
class OAuth2AuthenticationSuccessHandler(
    val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    val userRepository: UserRepository,
    val userTermsAgreementRepository: UserTermsAgreementRepository,
    val userTermsAgreementService: UserTermsAgreementService
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)

        if (response.isCommitted) return

        clearAuthenticationAttributes(request, response)

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        // 쿠키에서 아까 저장한 주소 꺼내기
        val redirectUri = CookieUtils.getCookie(
            request,
            HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME
        )?.value

        val targetUri = if(!redirectUri.isNullOrBlank()) redirectUri else defaultTargetUrl

        // 약관 미동의인 상태이면 리다이렉트
        val principal = authentication.principal as? CustomOAuth2User
        val email = principal?.attributes?.get("email") as? String

        if(email.isNullOrBlank()){
            return UriComponentsBuilder.fromUriString(targetUri)
                .build().toUriString()
        }

        // 유저가 이미 서비스에 존재하는지
        val existsUser = userRepository.findByEmail(email)

        // 약관 동의 여부는?
        val agree = existsUser?.id?.let {
            userTermsAgreementRepository.existsByUserId(it)
        } ?: false

        // 이미 약관 동의했으면 기존과 동일
        if(agree) return UriComponentsBuilder.fromUriString(targetUri)
            .build().toUriString()

        // 신규 고객은 토큰 발급
        val token = userTermsAgreementService.issueToken(
            TermsJwtPayload(
                email = email,
                name = principal.attributes["name"] as? String,
                picture = principal.attributes["picture"] as? String
            )
        )

        SecurityContextHolder.clearContext()
        request.getSession(false)?.invalidate()

        // 약관 페이지로 리다이렉트
        return UriComponentsBuilder.fromUriString("https://snuxi.com/terms")
            .queryParam("token", token)
            .queryParam("next", targetUri)
            .build()
            .toUriString()
    }

    private fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
    }
}