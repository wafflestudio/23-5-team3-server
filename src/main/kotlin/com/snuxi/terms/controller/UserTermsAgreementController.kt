package com.snuxi.terms.controller

import com.snuxi.security.CustomOAuth2User
import com.snuxi.terms.entity.UserTermsAgreement
import com.snuxi.terms.repository.UserTermsAgreementRepository
import com.snuxi.terms.service.UserTermsAgreementService
import com.snuxi.user.model.Role
import com.snuxi.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import com.snuxi.user.model.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/terms")
class UserTermsAgreementController (
    private val userTermsAgreementService: UserTermsAgreementService,
    private val userRepository: UserRepository,
    private val userTermsAgreementRepository: UserTermsAgreementRepository,
    private val securityContextRepository: SecurityContextRepository = HttpSessionSecurityContextRepository()
) {
    data class AgreeReq(
        val token: String,
        val termsVersion: BigDecimal
    )

    @PostMapping("/agree")
    fun agree(
        @RequestBody request: AgreeReq,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Any> {
        val payload = userTermsAgreementService.verify(request.token) ?: return ResponseEntity.badRequest().body(mapOf("message" to "유효하지 않은 토큰입니다. 재시도 해주세요."))

        // 동의하면, user 생성
        val user = userRepository.findByEmail(payload.email) ?: userRepository.save(
            User(
                email = payload.email,
                username = payload.name ?: "unknown",
                profileImageUrl = payload.picture
            )
        )

        // 약관동의 이력 저장
        userTermsAgreementRepository.save(
            UserTermsAgreement(
                userId = user.id!!,
                termsVersion = request.termsVersion,
                agreeAt = LocalDateTime.now(),
                ip = servletRequest.remoteAddr,
                userDevice = servletRequest.getHeader("User-Agent")
            )
        )

        // 세션 로그인
        val authorities = Collections.singleton(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        val principal = CustomOAuth2User(
            userId = requireNotNull(user.id),
            suspendedUntil = user.suspendedUntil,
            authorities = authorities,
            attributes = emptyMap(),
            nameAttributeKey = "id"
        )

        val auth = UsernamePasswordAuthenticationToken(principal, null, authorities)

        //세션 강제 생성
        servletRequest.getSession(true)

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        SecurityContextHolder.setContext(context)

        securityContextRepository.saveContext(context, servletRequest, servletResponse)
        return ResponseEntity.ok(mapOf("ok" to true, "userId" to user.id))
    }
}