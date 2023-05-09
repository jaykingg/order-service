package io.hobbyful.orderservice.config.api

import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@ConstructorBinding
@ConfigurationProperties(prefix = "api", ignoreUnknownFields = false)
@Validated
data class ApiConfigProperties(
    /**
     * 내부망 API hostname
     */
    @field: NotBlank
    @field: URL
    val privateHostname: String
)
