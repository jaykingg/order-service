package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.core.ErrorResponse
import io.hobbyful.orderservice.fixtures.*
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.payment.PaymentError
import io.hobbyful.orderservice.payment.PaymentStatus
import io.hobbyful.orderservice.payment.TossWebhookEventType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.Duration
import java.time.Instant
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class PaymentRefundWebhookCallbackIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository,
) : BehaviorSpec({
    val endpoint = "/orders/payment/toss-webhook"
    val customerId = UUID.randomUUID().toString()
    val request = webTestClient
        .post()
        .uri(endpoint)
        .contentType(MediaType.APPLICATION_JSON)

    Given("배송준비중 상태 주문 결제 취소를 위한 Webhook 전용 API") {
        When("payload 가 유효하지 않을 때") {
            val payload = checkoutPayload()

            Then("Response 400 BAD_REQUEST") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }

        When("Number(Payment orderId)가 존재하지 않는 경우") {
            val tossWebhookJsonBody = tossWebhookJson()

            Then("Response 404 NOT_FOUND") {
                request
                    .bodyValue(tossWebhookJsonBody)
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("토스페이먼츠 주문응답의 주문번호와 취소정보가 없을 경우") {
            val tossOrderId = "HF${Instant.now().toEpochMilli()}"
            val tossWebhookJsonBody = tossWebhookJson {
                eventType = TossWebhookEventType.PAYMENT_STATUS_CHANGED
                orderId = ""
            }

            val order = order {
                number = tossOrderId
                this.customerId = customerId
                status = OrderStatus.CANCELLED
                payment = payment {
                    orderId = tossOrderId
                }
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
                    .bodyValue(tossWebhookJsonBody)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody<ErrorResponse>()
                    .returnResult()
                    .responseBody!!
                    .should { body ->
                        body.message shouldBe PaymentError.INVALID_WEBHOOK_PAYLOAD.message
                        body.code shouldBe PaymentError.INVALID_WEBHOOK_PAYLOAD.code
                    }
            }

        }

        When("취소사유가 제공된 경우") {
            val tossOrderId = "HF${Instant.now().toEpochMilli()}"
            val cancelledReason = "취소사유"
            val tossWebhookJsonBody = tossWebhookJson {
                eventType = TossWebhookEventType.PAYMENT_STATUS_CHANGED
                orderId = tossOrderId
                status = PaymentStatus.CANCELED
                this.cancelledReason = cancelledReason
            }
            val order = order {
                number = tossOrderId
                this.customerId = customerId
                status = OrderStatus.PLANNING
                payment = payment {
                    orderId = tossOrderId
                }
                refund = null
                approvedAt = Instant.now().minus(Duration.ofHours(1))
            }

            beforeEach {
                orderRepository.save(order)
            }

            afterEach {
                orderRepository.deleteAll()
            }

            Then("Response 204 NO_CONTENT") {
                request
                    .bodyValue(tossWebhookJsonBody)
                    .exchange()
                    .expectStatus().isNoContent

                orderRepository.findByNumber(tossOrderId)
                    .shouldNotBeNull()
                    .should {
                        it.status shouldBe OrderStatus.CANCELLED
                        it.payment.shouldNotBeNull().cancels.shouldNotBeEmpty()
                        it.refund.shouldNotBeNull().reason shouldBe cancelledReason
                    }
            }
        }

        When("취소사유가 빈값인 경우") {
            val tossOrderId = "HF${Instant.now().toEpochMilli()}"
            val tossWebhookJsonBody = tossWebhookJson {
                eventType = TossWebhookEventType.PAYMENT_STATUS_CHANGED
                orderId = tossOrderId
                status = PaymentStatus.CANCELED
                cancelledReason = ""
            }
            val order = order {
                number = tossOrderId
                this.customerId = customerId
                status = OrderStatus.PLANNING
                payment = payment {
                    orderId = tossOrderId
                }
                refund = null
                approvedAt = Instant.now().minus(Duration.ofHours(1))
            }

            beforeEach {
                orderRepository.save(order)
            }

            afterEach {
                orderRepository.deleteAll()
            }

            Then("Response 204 NO_CONTENT") {
                request
                    .bodyValue(tossWebhookJsonBody)
                    .exchange()
                    .expectStatus().isNoContent

                orderRepository.findByNumber(tossOrderId)
                    .shouldNotBeNull()
                    .should {
                        it.status shouldBe OrderStatus.CANCELLED
                        it.payment.shouldNotBeNull().cancels.shouldNotBeEmpty()
                        it.refund.shouldNotBeNull().reason.shouldNotBeBlank()
                    }
            }
        }
    }
})
