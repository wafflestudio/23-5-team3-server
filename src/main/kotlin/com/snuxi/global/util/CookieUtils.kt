package com.snuxi.global.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.util.SerializationUtils
import java.util.*

object CookieUtils {

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

    fun serialize(obj: Any): String {
        return Base64.getUrlEncoder()
            .encodeToString(SerializationUtils.serialize(obj))
    }

    fun <T> deserialize(cookie: Cookie, cls: Class<T>): T {
        return cls.cast(
            SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.value)
            )
        )
    }
}