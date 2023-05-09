package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.core.ErrorResponse
import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.fixtures.adminStatusPayload
import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.order.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.should
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
class ChangeOrderStatusIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val endpoint = "/admin/orders/status"
    val customerId = UUID.randomUUID().toString()
    fun getOpaqueToken(authority: String = SecurityConstants.SERVICE_ADMIN) =
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(authority))
            .attributes { it["sub"] = customerId }

    Given("인증 실패") {
        When("권한이 없는 경우") {
            val request = webTestClient
                .post().uri("$endpoint/shipping")

            Then("Response 401 UNAUTHORIZED") {
                request
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }

        When("권한이 customer 인경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken("customer"))
                .post().uri("$endpoint/shipping")

            Then("Response 403 FORBIDDEN") {
                request
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        When("권한이 올바르지 않은 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken("unknown"))
                .post().uri("$endpoint/shipping")

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

        val shippingEndPoint = "$endpoint/shipping"
        val deliveredEndPoint = "$endpoint/delivered"

        When("PathVariable 이 OrderStatus 가 아닐 경우") {
            val payload = adminStatusPayload()
            Then("Response 400 BAD_REQUEST") {
                request
                    .uri("$endpoint/status")
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }

        When("payload 가 유효하지 않을 때") {
            val payload = adminStatusPayload {
                numbers = emptySet()
            }

            Then("Response 400 BAD_REQUEST") {
                request
                    .uri(shippingEndPoint)
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }

        When("요청된 target 주문상태가 올바르지 않은 경우") {
            And("target 주문상태가 [배송중,배송완료]가 아닌 경우") {
                val payload = adminStatusPayload()

                Then("Response 400 BAD_REQUEST") {
                    request
                        .uri("$endpoint/planning")
                        .bodyValue(payload)
                        .exchange()
                        .expectStatus().isBadRequest
                        .expectBody<ErrorResponse>()
                        .returnResult()
                        .responseBody!!
                        .should {
                            it.message shouldBe AdminError.INVALID_TARGET_STATUS_TO_CHANGE.message
                        }
                }
            }
        }

        When("Update 된 주문이 없을 경우") {
            Then("Response 400 BAD_REQUEST") {
                val payload = adminStatusPayload()

                request
                    .uri(shippingEndPoint)
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<OrderUpdateView>()
                    .returnResult()
                    .responseBody!!
                    .should {
                        it.totalCount shouldBe 0
                    }
            }
        }

        When("isOk") {
            And("배송 중으로 주문상태 변경이 요청될 경우") {
                val normalNumber1 = Instant.now().toEpochMilli().toString()
                val normalNumber2 = Instant.now().plusSeconds(1).toEpochMilli().toString()
                val abnormalNumber = Instant.now().plusSeconds(2).toEpochMilli().toString()

                val orderList = listOf(
                    order {
                        this.number = normalNumber1
                        status = OrderStatus.PAID
                        payment = paymentByStatus(status)
                    },
                    order {
                        this.number = normalNumber2
                        status = OrderStatus.PAID
                        payment = paymentByStatus(status)
                    },
                    order {
                        this.number = abnormalNumber
                        status = OrderStatus.PLANNING
                        payment = paymentByStatus(status)
                    })

                val payload = adminStatusPayload {
                    this.numbers = setOf(normalNumber1, normalNumber2, abnormalNumber)
                }

                beforeEach {
                    orderRepository.saveAll(orderList).collect()
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 200 OK - Update 한 주문 count") {
                    request
                        .uri(shippingEndPoint)
                        .bodyValue(payload)
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<OrderUpdateView>()
                        .returnResult()
                        .responseBody!!
                        .should {
                            it.totalCount shouldBe 1
                        }

                    val normalOrder1 = orderRepository.findByNumber(orderList[0].number)
                    val normalOrder2 = orderRepository.findByNumber(orderList[1].number)
                    val abnormalOrder = orderRepository.findByNumber(orderList[2].number)

                    normalOrder1!!.status shouldBe OrderStatus.PAID
                    normalOrder2!!.status shouldBe OrderStatus.PAID
                    abnormalOrder!!.status shouldBe OrderStatus.SHIPPING
                }
            }

            And("배송완료로 주문상태 변경이 요청될 경우") {
                val normalNumber1 = Instant.now().toEpochMilli().toString()
                val normalNumber2 = Instant.now().plusSeconds(1).toEpochMilli().toString()
                val abnormalNumber = Instant.now().plusSeconds(2).toEpochMilli().toString()

                val orderList = listOf(
                    order {
                        this.number = normalNumber1
                        status = OrderStatus.SHIPPING
                        payment = paymentByStatus(status)
                    },
                    order {
                        this.number = normalNumber2
                        status = OrderStatus.SHIPPING
                        payment = paymentByStatus(status)
                    },
                    order {
                        this.number = abnormalNumber
                        status = OrderStatus.PLANNING
                        payment = paymentByStatus(status)
                    })

                val payload = adminStatusPayload {
                    this.numbers = setOf(normalNumber1, normalNumber2, abnormalNumber)
                }

                beforeEach {
                    orderRepository.saveAll(orderList).collect()
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 200 OK - Update 한 주문 count") {
                    request
                        .uri(deliveredEndPoint)
                        .bodyValue(payload)
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<OrderUpdateView>()
                        .returnResult()
                        .responseBody!!
                        .should {
                            it.totalCount shouldBe 2
                        }


                    val normalOrder1 = orderRepository.findByNumber(orderList[0].number)
                    val normalOrder2 = orderRepository.findByNumber(orderList[1].number)
                    val abnormalOrder = orderRepository.findByNumber(orderList[2].number)

                    normalOrder1!!.status shouldBe OrderStatus.DELIVERED
                    normalOrder2!!.status shouldBe OrderStatus.DELIVERED
                    abnormalOrder!!.status shouldBe OrderStatus.PLANNING
                }
            }
        }

    }
})