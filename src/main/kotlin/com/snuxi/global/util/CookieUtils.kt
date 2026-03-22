package com.snuxi.global.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import java.util.*

object CookieUtils {

    private val objectMapper = jacksonObjectMapper()

    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        return request.cookies?.find { it.name == name }
    }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.domain = "snuxi.com"
        cookie.isHttpOnly = true
        cookie.maxAge = maxAge
        cookie.secure = true
        cookie.setAttribute("SameSite", "None")
        response.addCookie(cookie)
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        request.cookies?.find { it.name == name }?.let {
            val cookie = Cookie(name, null)
            cookie.path = "/"
            cookie.domain = "snuxi.com"
            cookie.maxAge = 0
            response.addCookie(cookie)
        }
    }

    /**
     * OAuth2AuthorizationRequest를 JSON으로 직렬화 (Java deserialization RCE 방지)
     */
    fun serialize(authRequest: OAuth2AuthorizationRequest): String {
        val data = mapOf(
            "authorizationUri" to authRequest.authorizationUri,
            "clientId" to authRequest.clientId,
            "redirectUri" to authRequest.redirectUri,
            "scopes" to authRequest.scopes.toList(),
            "state" to authRequest.state,
            "responseType" to authRequest.responseType.value,
            "additionalParameters" to authRequest.additionalParameters,
            "attributes" to authRequest.attributes,
            "authorizationRequestUri" to authRequest.authorizationRequestUri
        )
        val json = objectMapper.writeValueAsString(data)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())
    }

    /**
     * JSON에서 OAuth2AuthorizationRequest 복원
     */
    fun deserializeAuthRequest(cookie: Cookie): OAuth2AuthorizationRequest? {
        return try {
            val json = String(Base64.getUrlDecoder().decode(cookie.value))
            val data: Map<String, Any?> = objectMapper.readValue(json)

            @Suppress("UNCHECKED_CAST")
            OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(data["authorizationUri"] as String)
                .clientId(data["clientId"] as String)
                .redirectUri(data["redirectUri"] as? String)
                .scopes((data["scopes"] as? List<String>)?.toSet() ?: emptySet())
                .state(data["state"] as? String)
                .additionalParameters((data["additionalParameters"] as? Map<String, Any>) ?: emptyMap())
                .attributes { attrs ->
                    (data["attributes"] as? Map<String, Any>)?.let { attrs.putAll(it) }
                }
                .build()
        } catch (_: Exception) {
            null
        }
    }
}
