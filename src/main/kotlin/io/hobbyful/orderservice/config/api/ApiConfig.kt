package io.hobbyful.orderservice.config.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
@EnableConfigurationProperties(ApiConfigProperties::class)
class ApiConfig(
    private val config: ApiConfigProperties,
    private val objectMapper: ObjectMapper
) {
    @Bean
    fun apiClient() = WebClient.builder()
        .clientConnector(reactorClientHttpConnector("internal-api"))
        .baseUrl(config.privateHostname)
        .codecs {
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
        }
        .defaultHeaders {
            it.contentType = MediaType.APPLICATION_JSON
        }
        .build()

    fun reactorClientHttpConnector(name: String) =
        ReactorClientHttpConnector(
            HttpClient.create(
                ConnectionProvider.builder(name)
                    .maxConnections(500)
                    .maxIdleTime(Duration.ofSeconds(20))
                    .maxLifeTime(Duration.ofSeconds(60))
                    .pendingAcquireTimeout(Duration.ofSeconds(60))
                    .evictInBackground(Duration.ofSeconds(120))
                    .build()
            )
        )
}
