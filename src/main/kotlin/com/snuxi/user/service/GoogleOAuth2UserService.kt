package com.snuxi.user.service

import com.snuxi.user.model.User
import com.snuxi.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.NotSnuMailException
import com.snuxi.user.SuspendedUserException
import com.snuxi.user.model.Role
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections

@Service
class GoogleOAuth2UserService(
    private val userRepository: UserRepository,
    @Value("\${admin.emails}")
    private val adminEmails: String
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val attributes = oAuth2User.attributes

        val email = attributes["email"] as String
        if (!email.endsWith("@snu.ac.kr")) {
            throw NotSnuMailException()
        }

        val user = getOrSave(attributes)

        // 정지된 유저인지 체크
        if (user.suspendedUntil != null && user.suspendedUntil!!.isAfter(LocalDateTime.now())) {
            val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
            val formattedDate = user.suspendedUntil!!.format(formatter)

            throw SuspendedUserException(
                "계정이 정지되었습니다. ($formattedDate 까지 이용이 제한됩니다)"
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
        var user = userRepository.findByEmail(email)

        // 유저가 없으면 새로 생성
        if (user == null) {
            val name = attributes["name"] as String
            val picture = attributes["picture"] as? String

            user = userRepository.save(
                User(
                    email = email,
                    username = name,
                    profileImageUrl = picture
                )
            )
        }

        val adminList = adminEmails.split(",").map { it.trim() }

        // ADMIN으로 변경
        if (adminList.contains(email) && user.role != Role.ADMIN) {
            user.role = Role.ADMIN
            userRepository.save(user)
        }

        return user
    }

}