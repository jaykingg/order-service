package io.hobbyful.orderservice.config

import io.hobbyful.orderservice.core.CustomReactiveOpaqueTokenIntrospector
import io.hobbyful.orderservice.core.SecurityConstants
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class WebFluxSecurityConfig(
    private val properties: OAuth2ResourceServerProperties
) {
    private val corsConfig = CorsConfiguration().apply {
        allowedOrigins = listOf("*")
        allowedHeaders = listOf("*")
        allowedMethods = listOf("GET", "HEAD", "POST", "PUT", "DELETE")
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            authorizeExchange {
                authorize("/orders/payment/success", permitAll)
                authorize("/orders/payment/toss-webhook", permitAll)
                authorize("/admin/orders/**", hasAuthority(SecurityConstants.SERVICE_ADMIN))
                authorize("/orders/**", hasAuthority(SecurityConstants.CUSTOMER))
                authorize(anyExchange, permitAll)
            }
            oauth2ResourceServer {
                opaqueToken { }
            }
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/**", corsConfig)
    }

    @Bean
    fun introspector(): ReactiveOpaqueTokenIntrospector = CustomReactiveOpaqueTokenIntrospector(properties.opaquetoken)
}
