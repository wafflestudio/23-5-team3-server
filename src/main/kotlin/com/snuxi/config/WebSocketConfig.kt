package com.snuxi.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    // configureMessageBroker,
    // registerStompEndpoints override

    override fun configureMessageBroker(
        config: MessageBrokerRegistry
    ) {
        config.enableSimpleBroker("/sub")
        config.setApplicationDestinationPrefixes("/pub")
    }

    override fun registerStompEndpoints(
        registry: StompEndpointRegistry
    ) {
        registry.addEndpoint("/ws")
            .addInterceptors(HttpSessionHandshakeInterceptor())
            .setAllowedOriginPatterns(
                // 신뢰하는 프론트 도메인만 추가
                // 추후 다른 주소들 추가 필요
                "https://d2j21bk78krg0p.cloudfront.net"
            )
    }
}