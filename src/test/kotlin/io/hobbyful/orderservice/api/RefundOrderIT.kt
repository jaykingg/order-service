package io.hobbyful.orderservice.api

import com.github.tomakehurst.wiremock.WireMockServer
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returnsJson
import io.hobbyful.orderservice.core.ErrorResponse
import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.fixtures.*
import io.hobbyful.orderservice.order.OrderError
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.total
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8090)
@Import(TestChannelBinderConfiguration::class)
class RefundOrderIT(
    private val webTestClient: WebTestClient,
    private val wireMock: WireMockServer,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val inquiryOrderId = ObjectId.get()
    val endpoint = "/orders/$inquiryOrderId/refund"
    val tokenSubject = UUID.randomUUID().toString()
    fun requestWith(token: SecurityMockServerConfigurers.OpaqueTokenMutator) = webTestClient
        .mutateWith(token)
        .post().uri(endpoint)

    val request = requestWith(
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
            .attributes { it["sub"] = tokenSubject }
    )

    afterEach {
        clearAllMocks()
    }

    Given("인증") {
        When("Token 이 없는 경우") {
            Then("Response 401 UNAUTHORIZED") {
                webTestClient
                    .post().uri(endpoint)
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }
    }

    Given("payload 유효성") {
        When("payload 가 유효하지 않은 경우") {
            val payload = refundPayload {
                reason = ""
            }
            Then("Response 400 BAD_REQUEST") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }
    }

    Given("결제 취소") {
        When("해당 주문 OrderId 가 존재하지 않는 경우") {
            val payload = refundPayload()
            Then("Response 404 NOT_FOUND") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("해당 주문이 결제 상태가 아닌 경우") {
            val payload = refundPayload()

            And("주문 내역이 없는 경우") {
                val order = order {
                    id = inquiryOrderId
                    customerId = tokenSubject
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
                            body.message shouldBe OrderError.ORDER_NOT_REFUNDABLE.message
                            body.code shouldBe OrderError.ORDER_NOT_REFUNDABLE.code
                        }

                }
            }

            And("결제(지불) 상태이지 않은 경우") {
                val order = order {
                    id = inquiryOrderId
                    customerId = tokenSubject
                    status = OrderStatus.PENDING
                    payment = payment()
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
                            body.message shouldBe OrderError.ORDER_NOT_REFUNDABLE.message
                            body.code shouldBe OrderError.ORDER_NOT_REFUNDABLE.code
                        }

                }
            }

            And("이미 취소 상태인 경우") {
                val order = order {
                    id = inquiryOrderId
                    customerId = tokenSubject
                    storeCredit = storeCredit {
                        transaction = transactionView()
                    }
                    status = OrderStatus.CANCELLED
                    payment = payment()
                    refund = refund()
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
                            body.message shouldBe OrderError.ORDER_ALREADY_REFUNDED.message
                            body.code shouldBe OrderError.ORDER_ALREADY_REFUNDED.code
                        }

                }
            }
        }

        When("결제 취소 요청에 실패한 경우") {
            val error = "test-toss-cancel-error"
            val message = "test-toss-cancel-message"
            val payload = refundPayload()
            val order = order {
                id = inquiryOrderId
                customerId = tokenSubject
                storeCredit = storeCredit {
                    transaction = transactionView()
                }
                status = OrderStatus.PAID
                payment = payment()
            }

            beforeEach {
                orderRepository.save(order)
                wireMock.post {
                    url equalTo "/payments/${order.payment!!.paymentKey}/cancel"
                    headers contains HttpHeaders.CONTENT_TYPE equalTo MediaType.APPLICATION_JSON_VALUE
                } returnsJson {
                    statusCode = HttpStatus.BAD_REQUEST.value()
                    body = """
                        {
                          "code": "$error",
                          "message": "$message"
                        }
                    """.trimIndent()
                }
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
                    .message shouldBe message
            }
        }

        When("결제 취소 성공") {
            val payload = refundPayload()
            val order = order {
                id = inquiryOrderId
                customerId = tokenSubject
                storeCredit = storeCredit {
                    transaction = transactionView()
                }
                status = OrderStatus.PAID
                payment = payment()
            }
            val paymentJsonBody = paymentJson {
                orderId = order.id!!
                totalAmount = order.total
            }

            beforeEach {
                orderRepository.save(order)
                wireMock.post {
                    url equalTo "/payments/${order.payment!!.paymentKey}/cancel"
                    headers contains HttpHeaders.CONTENT_TYPE equalTo MediaType.APPLICATION_JSON_VALUE
                } returnsJson {
                    body = paymentJsonBody
                }
            }

            afterEach {
                orderRepository.deleteAll()
            }

            Then("Response 204 NO_CONTENT") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent

                val resultOrder = orderRepository.findById(order.id!!)
                resultOrder?.status shouldBe OrderStatus.CANCELLED
                resultOrder?.refund?.reason shouldBe "test-refundPayload-reason"
            }
        }
    }
})