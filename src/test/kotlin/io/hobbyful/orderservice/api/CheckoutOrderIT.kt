package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.core.ErrorResponse
import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.fixtures.checkoutPayload
import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.storeCredit
import io.hobbyful.orderservice.fixtures.summary
import io.hobbyful.orderservice.order.OrderError
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.total
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
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
class CheckoutOrderIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val orderId = ObjectId.get()
    val endPoint = "/orders/$orderId/checkout"
    val customerId = UUID.randomUUID().toString()
    fun getOpaqueToken(authority: String = SecurityConstants.CUSTOMER) = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority(authority))
        .attributes { it["sub"] = customerId }

    Given("주문 결제 요청 인증 실패") {
        When("인증이 없는 경우") {
            val request = webTestClient
                .post().uri(endPoint)
            val payload = checkoutPayload()

            Then("Response 401 UNAUTHORIZED") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }

        When("권한이 'customer' 가 아닌 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken("unknown"))
                .post().uri(endPoint)
            val payload = checkoutPayload()

            Then("Response 403 FORBIDDEN") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isForbidden
            }
        }
    }

    Given("주문 결제 요청 인증 성공") {
        val request = webTestClient
            .mutateWith(getOpaqueToken("customer"))
            .post().uri(endPoint)

        When("payload 가 유효하지 않을 때") {
            val payload = checkoutPayload {
                storeCreditAmount = -1
            }

            Then("Response 400 BAD_REQUEST") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }

        When("orderId 가 존재하지 않는 경우") {
            val payload = checkoutPayload()

            Then("Response 400 BAD_REQUEST") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("주문대기 상태가 아닌 경우") {
            val payload = checkoutPayload()
            val order = order {
                id = orderId
                this.customerId = customerId
                storeCredit = storeCredit()
                status = OrderStatus.CHECKOUT
            }

            beforeEach {
                orderRepository.save(order)
            }

            afterEach {
                orderRepository.deleteAll()
            }

            Then("Response 400 BAD_REQUEST") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody<ErrorResponse>()
                    .returnResult()
                    .responseBody!!
                    .should { body ->
                        body.message shouldBe OrderError.ORDER_NOT_PENDING.message
                        body.code shouldBe OrderError.ORDER_NOT_PENDING.code
                    }
            }
        }

        When("사용할 적립금이 주문 총액보다 클 경우") {
            val order = order {
                id = orderId
                this.customerId = customerId
            }
            val payload = checkoutPayload {
                storeCreditAmount = order.total + 1000
            }

            beforeEach {
                orderRepository.save(order)
            }

            afterEach {
                orderRepository.deleteAll()
            }

            Then("Response 400 BAD_REQUEST") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody<ErrorResponse>()
                    .returnResult()
                    .responseBody!!
                    .should { body ->
                        body.message shouldBe OrderError.STORE_CREDIT_EXCEEDS_ORDER_TOTAL.message
                        body.code shouldBe OrderError.STORE_CREDIT_EXCEEDS_ORDER_TOTAL.code
                    }
            }
        }

        When("isOk") {
            val payload = checkoutPayload()
            val order = order {
                id = orderId
                this.customerId = customerId
                summary = summary {
                    storeCreditAmount = payload.storeCreditAmount
                    total = 10000
                }
            }

            beforeEach {
                orderRepository.save(order)
            }

            afterEach {
                orderRepository.deleteAll()
            }

            Then("Response 200 OK") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isOk

                val result = orderRepository.findByIdAndCustomerId(
                    order.id!!,
                    order.customerId
                )

                result!!.status shouldBe OrderStatus.CHECKOUT
            }
        }
    }
})