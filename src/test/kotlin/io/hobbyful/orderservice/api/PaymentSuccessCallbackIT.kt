package io.hobbyful.orderservice.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returnsJson
import io.hobbyful.orderservice.core.ErrorResponse
import io.hobbyful.orderservice.fixtures.*
import io.hobbyful.orderservice.order.OrderError
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.total
import io.hobbyful.orderservice.payment.Payment
import io.hobbyful.orderservice.payment.PaymentError
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
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
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8090)
@Import(TestChannelBinderConfiguration::class)
class PaymentSuccessCallbackIT(
    private val webTestClient: WebTestClient,
    private val wireMock: WireMockServer,
    private val orderRepository: OrderRepository,
    private val mapper: ObjectMapper,
) : BehaviorSpec({
    val endpoint = "/orders/payment/success"
    val parameterObject = confirmPaymentParameters()

    val request = webTestClient
        .get()
        .uri { uriBuilder ->
            uriBuilder.path(endpoint)
                .queryParam("paymentKey", parameterObject.paymentKey)
                .queryParam("orderId", parameterObject.orderId)
                .queryParam("amount", parameterObject.amount)
                .build()
        }

    afterEach {
        clearAllMocks()
    }

    Given("ParameterObject 유효성") {
        When("ParameterObject 가 유효하지 않은 경우") {
            val invalidParameterObject = confirmPaymentParameters {
                paymentKey = ""
                orderId = ""
                amount = -1
            }

            Then("Response 400 BAD_REQUEST") {
                webTestClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path(endpoint)
                            .queryParam("paymentKey", invalidParameterObject.paymentKey)
                            .queryParam("orderId", invalidParameterObject.orderId)
                            .queryParam("amount", invalidParameterObject.amount)
                            .build()
                    }
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }
    }

    Given("승인 요청") {
        When("orderId로 주문이 존재하지 않는 경우") {
            Then("Response 400 BAD_REQUEST") {
                request
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        And("주문이 결제요청 조건을 만족하지 못하는 경우") {
            When("주문이 결제요청 상태가 아닌 경우") {
                val order = order {
                    number = parameterObject.orderId
                    status = OrderStatus.PAID
                    payment = payment {
                        paymentKey = parameterObject.paymentKey
                    }
                }

                beforeEach {
                    orderRepository.save(order)
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 400 BAD_REQUEST") {
                    request
                        .exchange()
                        .expectStatus().isBadRequest
                        .expectBody<ErrorResponse>()
                        .returnResult()
                        .responseBody!!
                        .should { body ->
                            body.message shouldBe OrderError.INVALID_ORDER_TO_CHECKOUT.message
                            body.code shouldBe OrderError.INVALID_ORDER_TO_CHECKOUT.code
                        }
                }
            }

            When("주문이 결제요청이고 결제 총액이 일치하지 않는 경우") {
                val order = order {
                    number = parameterObject.orderId
                    status = OrderStatus.CHECKOUT
                    summary = summary {
                        total = parameterObject.amount + 1
                    }
                    payment = payment {
                        paymentKey = parameterObject.paymentKey
                    }
                }

                beforeEach {
                    orderRepository.save(order)
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 400 BAD_REQUEST") {
                    request
                        .exchange()
                        .expectStatus().isBadRequest
                        .expectBody<ErrorResponse>()
                        .returnResult()
                        .responseBody!!
                        .should { body ->
                            body.message shouldBe PaymentError.INVALID_PAYMENT_AMOUNT.message
                            body.code shouldBe PaymentError.INVALID_PAYMENT_AMOUNT.code
                        }
                }
            }
        }

        And("주문이 결제요청 조건을 만족하는 경우") {
            When("적립금 차감 Internal API 호출에 실패 한 경우") {
                val order = order {
                    number = parameterObject.orderId
                    status = OrderStatus.CHECKOUT
                    storeCredit = storeCredit {
                        amount = 1000
                    }
                    summary = summary {
                        total = parameterObject.amount
                    }
                    payment = payment {
                        paymentKey = parameterObject.paymentKey
                    }
                }

                beforeEach {
                    orderRepository.save(order)
                    wireMock.post {
                        url equalTo "/internal/membership/store-credit/${order.customerId}/charge"
                        headers contains HttpHeaders.CONTENT_TYPE equalTo MediaType.APPLICATION_JSON_VALUE
                    } returnsJson {
                        statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
                        body = """
                        {
                          "code": "ErrorCode",
                          "message": "ErrorMessage"
                        }
                    """.trimIndent()
                    }
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 400 BAD_REQUEST") {
                    request
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            When("토스페이먼츠의 결제 승인 요청에 실패한 경우") {
                val errorCode = "test-toss-errorCode"
                val errorMessage = "test-toss-errorMessage"

                val order = order {
                    id = ObjectId.get()
                    number = parameterObject.orderId
                    status = OrderStatus.CHECKOUT
                    summary = summary {
                        total = parameterObject.amount
                    }
                    payment = payment {
                        paymentKey = parameterObject.paymentKey
                    }
                }
                val chargeStoreCreditResponseJson = chargeStoreCreditResponseJson {
                    orderId = order.id!!
                    customerId = order.customerId
                }

                beforeEach {
                    orderRepository.save(order)
                    wireMock.post {
                        url equalTo "/internal/membership/store-credit/account/${order.customerId}/charge"
                        headers contains HttpHeaders.CONTENT_TYPE equalTo MediaType.APPLICATION_JSON_VALUE
                    } returnsJson {
                        body = chargeStoreCreditResponseJson
                    }
                    wireMock.post {
                        url equalTo "/payments/confirm"
                        headers contains HttpHeaders.CONTENT_TYPE equalTo MediaType.APPLICATION_JSON_VALUE
                    } returnsJson {
                        statusCode = HttpStatus.BAD_REQUEST.value()
                        body = """
                        {
                          "code": "$errorCode",
                          "message": "$errorMessage"
                        }
                    """.trimIndent()
                    }
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 400 BAD_REQUEST") {
                    request
                        .exchange()
                        .expectStatus().isBadRequest
                        .expectBody<ErrorResponse>()
                        .returnResult()
                        .responseBody!!
                        .should { body ->
                            body.message shouldBe errorMessage
                            body.code shouldBe errorCode
                        }
                }
            }

            When("토스페이먼츠의 결제 승인 요청이 성공한 경우") {
                val order = order {
                    id = ObjectId.get()
                    number = parameterObject.orderId
                    status = OrderStatus.CHECKOUT
                    storeCredit = storeCredit {
                        amount = 1000
                    }
                    summary = summary {
                        total = parameterObject.amount
                    }
                    payment = payment {
                        paymentKey = parameterObject.paymentKey
                    }
                }

                val transactionId = ObjectId.get()
                val chargeStoreCreditResponseJson = chargeStoreCreditResponseJson {
                    this.transactionId = transactionId
                    orderId = order.id!!
                    customerId = order.customerId
                    amount = order.storeCredit.amount
                }

                val paymentJsonBody = paymentJson {
                    orderId = order.id!!
                    totalAmount = order.total
                }

                beforeEach {
                    orderRepository.save(order)
                    wireMock.post {
                        url equalTo "/internal/membership/store-credit/account/${order.customerId}/charge"
                        headers contains HttpHeaders.CONTENT_TYPE equalTo MediaType.APPLICATION_JSON_VALUE
                    } returnsJson {
                        body = chargeStoreCreditResponseJson
                    }
                    wireMock.post {
                        url equalTo "/payments/confirm"
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
                        .exchange()
                        .expectStatus().isNoContent

                    orderRepository.findById(order.id!!)?.let {
                        it.storeCredit.transaction!!.id shouldBe transactionId
                        it.storeCredit.amount shouldBe order.storeCredit.amount
                        it.payment!! shouldBeEqualToComparingFields mapper.readValue(
                            paymentJsonBody,
                            Payment::class.java
                        )
                        it.status shouldBe OrderStatus.PAID
                        it.placedAt.shouldNotBeNull()
                    }
                }
            }
        }
    }
})