package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderView
import io.kotest.core.spec.style.BehaviorSpec
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class GetOrderByIdIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val tokenSubject = UUID.randomUUID().toString()
    val opaqueToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority("customer"))
        .attributes { it["sub"] = tokenSubject }

    Given("GET /orders/{orderId} - 주문 상세 정보 조회") {
        val orderId = ObjectId.get()
        val request = webTestClient
            .mutateWith(opaqueToken)
            .get().uri("/orders/{orderId}", orderId)

        When("orderId로 고객의 주문이 존재하지 않을때") {
            Then("404 Not Found") {
                request
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("orderId로 고객의 주문이 존재할때") {
            beforeEach {
                orderRepository.save(
                    order {
                        id = orderId
                        customerId = tokenSubject
                    }
                )
            }

            afterEach {
                orderRepository.deleteAll()
            }

            Then("200 Ok") {
                request
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<OrderView>()
                    .returnResult()
            }
        }
    }
})
