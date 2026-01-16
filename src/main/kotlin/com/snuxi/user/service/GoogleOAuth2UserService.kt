package com.snuxi.user.service

import com.snuxi.user.model.User
import com.snuxi.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
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
        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        return DefaultOAuth2User(
            Collections.singleton(SimpleGrantedAuthority("ROLE_${user.role.name}")),
            attributes,
            userNameAttributeName
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