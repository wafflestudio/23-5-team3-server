package com.snuxi.admin.service

import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.repository.UserRepository
import com.snuxi.user.UserNotFoundException
import org.springframework.security.core.session.SessionRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val sessionRegistry: SessionRegistry
) {

    @Transactional
    fun suspendUser(targetUserId: Long, days: Long) {
        // 유저 조회
        val user = userRepository.findById(targetUserId)
            .orElseThrow { UserNotFoundException() }

        // DB 업데이트 (정지 기간 & 횟수)
        user.suspendedUntil = LocalDateTime.now().plusDays(days)
        user.suspensionCount += 1

        // 모든 접속자 명단 가져오기
        val principals = sessionRegistry.allPrincipals

        for (principal in principals) {
            if (principal is CustomOAuth2User && principal.userId == targetUserId) {
                // 이 유저의 모든 세션 가져오기
                val sessions = sessionRegistry.getAllSessions(principal, false)

                // 세션 폭파 (다음 요청 시 로그아웃됨)
                for (session in sessions) {
                    session.expireNow()
                }
            }
        }
    }
}