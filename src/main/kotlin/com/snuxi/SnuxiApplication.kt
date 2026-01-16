package com.snuxi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration

//exclude = 테스트 용. 개발 후 삭제 필요
@SpringBootApplication()
class SnuxiApplication

fun main(args: Array<String>) {
	runApplication<SnuxiApplication>(*args)
}
