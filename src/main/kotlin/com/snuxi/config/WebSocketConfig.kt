package com.snuxi.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val stompMessageAuthInterceptor: StompMessageAuthInterceptor
) : WebSocketMessageBrokerConfigurer {
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
                "https://d2j21bk78krg0p.cloudfront.net",
                "http://localhost:5173",
                "https://d2c0wdnl0iqvgb.cloudfront.net",
                "https://snuxi.com" //프론트 배포 도메인
            )
            .withSockJS()
    }

    // 추가로, 해당 팟 외부 사람들은 메시지를 볼 수 없게 하기위해
    // 클라이언트에서 보낸 메시지가 서버 도달 전에 특정 interceptor 거치도록 함
    override fun configureClientInboundChannel(
        channelRegistration: ChannelRegistration
    ) {
        channelRegistration.interceptors(stompMessageAuthInterceptor)
    }
}