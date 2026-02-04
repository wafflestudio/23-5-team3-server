package com.snuxi.user.service

import com.snuxi.user.model.User
import com.snuxi.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.SuspendedUserException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Collections

@Service
class GoogleOAuth2UserService(
    private val userRepository: UserRepository,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val attributes = oAuth2User.attributes

        val email = attributes["email"] as String
        if (!email.endsWith("@snu.ac.kr")) {
            throw OAuth2AuthenticationException(
                OAuth2Error("Email is not valid"),
                "서울대학교(@snu.ac.kr) 계정만 로그인 가능합니다."
            )
        }

        val user = getOrSave(attributes)

        // 정지된 유저인지 체크
        if (user.suspendedUntil != null && user.suspendedUntil!!.isAfter(LocalDateTime.now())) {
            throw SuspendedUserException(
                "계정이 정지되었습니다. (누적 ${user.suspensionCount}회, ${user.suspendedUntil}까지 이용 불가)"
            )
        }

        val userNameAttributeName =
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        val authorities = Collections.singleton(SimpleGrantedAuthority("ROLE_${user.role.name}"))

        return CustomOAuth2User(
            userId = requireNotNull(user.id) { "DB 저장 후 유저 ID를 생성하지 못했습니다." },
            suspendedUntil = user.suspendedUntil,
            authorities = authorities,
            attributes = attributes,
            nameAttributeKey = userNameAttributeName
        )
    }

    private fun getOrSave(attributes: Map<String, Any>): User {
        val email = attributes["email"] as String

        val existingUser = userRepository.findByEmail(email)

        if(existingUser != null) {
            return existingUser
        }

        val name = attributes["name"] as String
        val picture = attributes["picture"] as? String

        return userRepository.save(
            User(
                email = email,
                username = name,
                profileImageUrl = picture
            )
        )
    }

}