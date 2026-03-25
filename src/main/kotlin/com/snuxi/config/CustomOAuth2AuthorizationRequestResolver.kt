package com.snuxi.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class CustomOAuth2AuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver =
        DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization")

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request) ?: return null
        return customizeAuthorizationRequest(authorizationRequest)
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request, clientRegistrationId) ?: return null
        return customizeAuthorizationRequest(authorizationRequest)
    }

    private fun customizeAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest,
    ): OAuth2AuthorizationRequest {
        val registrationId = authorizationRequest.attributes["registration_id"] as? String
        if (registrationId != "google") {
            return authorizationRequest
        }

        val additionalParameters = authorizationRequest.additionalParameters.toMutableMap()
        additionalParameters["prompt"] = "select_account"
        additionalParameters["hd"] = "snu.ac.kr"

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .build()
    }
}
