package io.hobbyful.orderservice.config

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class Jackson2ObjectMapperConfig {
    /**
     * `ObjectId` to `String` serialization 설정을 위해 Custom Jackson ObjectMapper 제공
     *
     * [WebFluxConfigurer.configureHttpMessageCodecs]에 기본 `ServerCodec` 설정 주입 필요
     */
    @Bean
    fun jackson2ObjectMapperBuilder() = Jackson2ObjectMapperBuilderCustomizer { builder ->
        builder.serializerByType(ObjectId::class.java, ToStringSerializer())
    }
}
