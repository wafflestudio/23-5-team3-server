package com.snuxi.config

import com.snuxi.user.service.GoogleOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    val googleOAuth2UserService: GoogleOAuth2UserService
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .headers { it.frameOptions { frameOptions -> frameOptions.sameOrigin() } }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/login", "/h2-console/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login {
                it.loginPage("/login")
                it.userInfoEndpoint {
                    endpoint ->
                        endpoint.userService(googleOAuth2UserService)
                }
                it.defaultSuccessUrl("/", true)
            }
            .logout {
                it.logoutSuccessUrl("/")
                it.logoutUrl("/logout")
                it.invalidateHttpSession(true)
            }
        return http.build()
    }



}