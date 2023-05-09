package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.payment
import io.hobbyful.orderservice.fixtures.storeCredit
import io.hobbyful.orderservice.fixtures.transactionView
import io.hobbyful.orderservice.order.OrderListView
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.kotest.core.spec.style.BehaviorSpec
import kotlinx.coroutines.flow.collect
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.time.Instant
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class GetOrdersIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val tokenSubject = UUID.randomUUID().toString()
    val opaqueToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority("customer"))
        .attributes { it["sub"] = tokenSubject }

    Given("GET /orders - 결제한 모든 주문 조회") {
        val request = webTestClient
            .mutateWith(opaqueToken)
            .get().uri("/orders")

        When("결제한 주문이 존재하지 않을때") {
            beforeEach {
                orderRepository.saveAll(
                    listOf(
                        order {
                            number = "1"
                            customerId = tokenSubject
                            storeCredit = storeCredit {
                                transaction = transactionView()
                            }
                            status = OrderStatus.PENDING
                        },
                        order {
                            number = "2"
                            customerId = tokenSubject
                            storeCredit = storeCredit {
                                transaction = transactionView()
                            }
                            status = OrderStatus.CHECKOUT
                        }
                    )
                ).collect()
            }

            afterEach { orderRepository.deleteAll() }

            Then("200 Ok - Empty JSON Array") {
                request
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
            }
        }

        When("결제된 주문이 존재할때") {
            beforeEach {
                orderRepository.saveAll(
                    listOf(
                        order {
                            number = "1"
                            customerId = tokenSubject
                            storeCredit = storeCredit {
                                transaction = transactionView()
                            }
                            status = OrderStatus.PENDING
                        },
                        order {
                            number = "2"
                            customerId = tokenSubject
                            storeCredit = storeCredit {
                                transaction = transactionView()
                            }
                            status = OrderStatus.CHECKOUT
                        },
                        order {
                            number = "3"
                            customerId = tokenSubject
                            storeCredit = storeCredit {
                                transaction = transactionView()
                            }
                            status = OrderStatus.PAID
                            payment = payment {
                                number = Instant.now().plusSeconds(1000000).toEpochMilli().toString()
                                totalAmount = 30_000
                                approvedAt = Instant.now()
                            }
                        },
                        order {
                            number = "4"
                            customerId = tokenSubject
                            storeCredit = storeCredit {
                                transaction = transactionView()
                            }
                            status = OrderStatus.PAID
                            payment = payment {
                                number = Instant.now().toEpochMilli().toString()
                                totalAmount = 10_000
                                approvedAt = Instant.now()
                            }
                        },
                        order {
                            number = "5"
                            customerId = tokenSubject
                            storeCredit = storeCredit {
                                transaction = transactionView()
                            }
                            status = OrderStatus.PAID
                            payment = payment {
                                number = Instant.now().plusSeconds(1000).toEpochMilli().toString()
                                totalAmount = 40_000
                                approvedAt = Instant.now()
                            }
                        }
                    )
                ).collect()
            }

            afterEach { orderRepository.deleteAll() }

            Then("200 Ok - 고객이 결제한 모든 주문 리스트") {
                request
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<OrderListView>()
                    .hasSize(3)
                    .returnResult()
                    .responseBody!!
                    .sortedByDescending { it.number }
            }
        }
    }
})
