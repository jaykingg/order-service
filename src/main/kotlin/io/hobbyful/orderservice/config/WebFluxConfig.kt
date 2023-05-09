package io.hobbyful.orderservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.hobbyful.orderservice.core.CustomStringToEnumConverterFactory
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.format.FormatterRegistry
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

@Configuration
@EnableWebFlux
class WebFluxConfig(
    private val objectMapper: ObjectMapper
) : WebFluxConfigurer {
    /**
     * [Jackson2ObjectMapperBuilderCustomizer] 기반 Custom Jackson2ObjectMapper 활성화를 위해 default codec 설정
     */
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
    }

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(ReactivePageableHandlerMethodArgumentResolver())
    }

    /**
     * 문자열 -> Enum 타입 변환 로직 등록
     */
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverterFactory(CustomStringToEnumConverterFactory())
    }
}
