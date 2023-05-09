package io.hobbyful.orderservice.tossPayments

import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

/**
 * 토스페이먼츠 클라이언트 환경설정
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "toss-payments", ignoreUnknownFields = false)
@Validated
data class TossPaymentsProperties(
    /**
     * 토스페이먼츠 API의 base url
     */
    @field: NotBlank
    @field: URL
    val baseUrl: String,

    /**
     * 토스페이먼츠 API 호출 인증을 위한 시크릿 키
     */
    @field: NotBlank
    val secret: String
)
