package io.hobbyful.orderservice.config

import io.hobbyful.orderservice.config.api.ApiConfig
import io.hobbyful.orderservice.tossPayments.TossPaymentsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(TossPaymentsProperties::class)
class TossPaymentsClientConfig(
    private val tossPaymentsProperties: TossPaymentsProperties,
    private val apiConfig: ApiConfig
) {
    @Bean
    fun tossPaymentsClient(): WebClient =
        WebClient.builder()
            .clientConnector(apiConfig.reactorClientHttpConnector("toss-payments"))
            .baseUrl(tossPaymentsProperties.baseUrl)
            .defaultHeaders {
                it.contentType = MediaType.APPLICATION_JSON
                it.setBasicAuth(tossPaymentsProperties.secret, "")
            }
            .build()
}
