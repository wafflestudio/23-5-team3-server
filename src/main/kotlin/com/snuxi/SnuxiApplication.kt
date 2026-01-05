package com.snuxi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnuxiApplication

fun main(args: Array<String>) {
	runApplication<SnuxiApplication>(*args)
}
