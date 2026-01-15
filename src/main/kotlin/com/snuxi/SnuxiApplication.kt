package com.snuxi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration // 이 줄 추가
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

//DB연결 미루고 s3 테스트 용. 나중에 exclude 부분 지워야함
@SpringBootApplication()
class SnuxiApplication

fun main(args: Array<String>) {
	runApplication<SnuxiApplication>(*args)
}
