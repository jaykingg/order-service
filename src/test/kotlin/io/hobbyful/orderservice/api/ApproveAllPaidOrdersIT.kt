package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.payment
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.OrderUpdateView
import io.hobbyful.orderservice.payment.PaymentStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.Instant
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class ApproveAllPaidOrdersIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val endpoint = "/admin/orders/approve"
    val customerId = UUID.randomUUID().toString()
    fun getOpaqueToken(authority: String = SecurityConstants.SERVICE_ADMIN) =
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(authority))
            .attributes { it["sub"] = customerId }

    Given("인증 실패") {
        When("권한이 없는 경우") {
            val request = webTestClient
                .post().uri(endpoint)

            Then("Response 401 UNAUTHORIZED") {
                request
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }

        When("권한이 customer 인경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken("customer"))
                .post().uri(endpoint)

            Then("Response 403 FORBIDDEN") {
                request
                    .exchange()
                    .expectStatus().isForbidden
            }
        }
    }

    Given("인증 성공") {
        val request = webTestClient
            .mutateWith(getOpaqueToken())
            .post()
            .uri(endpoint)

        When("isOk") {
            And("마감 조건에 일치하는 주문이 없는 경우") {
                val orders = List(10) { index ->
                    order {
                        number = Instant.now().plusSeconds(index.toLong()).toString()
                    }
                }

                beforeEach {
                    orderRepository.saveAll(orders).collect()
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("TotalCount 0 - Response 200 OK") {
                    request
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<OrderUpdateView>()
                        .returnResult()
                        .shouldNotBeNull()
                        .responseBody!!
                        .totalCount shouldBe 0
                }
            }

            And("마감 조건에 일치하는 주문이 존재하는 경우") {
                val normalOrders = List(5) { index ->
                    order {
                        number = Instant.now().plusSeconds(index.toLong()).toString()
                    }
                }

                val willBeApprovedOrders = List(10) { index ->
                    order {
                        number = Instant.now().plusSeconds(index.toLong()).toString()
                        status = OrderStatus.PAID
                        payment = payment {
                            status = PaymentStatus.DONE
                        }
                    }
                }

                beforeEach {
                    orderRepository.saveAll(normalOrders).collect()
                    orderRepository.saveAll(willBeApprovedOrders).collect()
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("TotalCount Orders - Response 200 OK") {
                    request
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<OrderUpdateView>()
                        .returnResult()
                        .responseBody
                        .shouldNotBeNull()
                        .totalCount shouldBe willBeApprovedOrders.size

                    val approvedOrderNumbers = willBeApprovedOrders.map { it.number }
                    approvedOrderNumbers.forEach {
                        val order = orderRepository.findByNumber(it)!!
                        order.status shouldBe OrderStatus.PLANNING
                        order.approvedAt.shouldNotBeNull()
                        order.updatedAt shouldBe order.approvedAt
                    }
                }
            }
        }
    }
})
