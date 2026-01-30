package com.snuxi.config

import com.snuxi.user.service.GoogleOAuth2UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.security.config.Customizer
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    val googleOAuth2UserService: GoogleOAuth2UserService,
    val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    val customLogoutSuccessHandler: CustomLogoutSuccessHandler

) {
    @Bean
    fun filterChain(
        http: HttpSecurity,
    ): SecurityFilterChain {
        http
            .cors ( Customizer.withDefaults() )
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/",
                    "/login",
                    "/error",
                    "/maps/landmarks",
                    "/rooms/search",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/favicon.ico",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/maps/landmarks",
                    "/rooms/search").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login {
                it.loginPage("/login")
                it.authorizationEndpoint { endpoint ->
                    endpoint.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                }
                it.userInfoEndpoint {
                    endpoint ->
                        endpoint.userService(googleOAuth2UserService)
                }
                it.successHandler(oAuth2AuthenticationSuccessHandler)
            }
            .logout {
                it.logoutRequestMatcher(AntPathRequestMatcher("/logout"))
                it.logoutUrl("/logout")
                it.logoutSuccessHandler(customLogoutSuccessHandler)
                it.invalidateHttpSession(true)
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:5173", "https://d2c0wdnl0iqvgb.cloudfront.net", "https://snuxi.com")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }



}
