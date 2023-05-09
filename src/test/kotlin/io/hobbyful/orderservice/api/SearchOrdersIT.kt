package io.hobbyful.orderservice.api

import com.ninjasquad.springmockk.MockkBean
import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.payment
import io.hobbyful.orderservice.fixtures.refund
import io.hobbyful.orderservice.fixtures.shippingInfo
import io.hobbyful.orderservice.order.Order
import io.hobbyful.orderservice.order.OrderQueryService
import io.hobbyful.orderservice.order.OrderStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.time.Instant
import java.time.Period
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class SearchOrdersIT(
    private val webTestClient: WebTestClient,
    @MockkBean private val orderQueryService: OrderQueryService
) : BehaviorSpec({
    val endpoint = "/admin/orders/search"
    val customerId = UUID.randomUUID().toString()
    fun getOpaqueToken(authority: String = SecurityConstants.SERVICE_ADMIN) =
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(authority))
            .attributes { it["sub"] = customerId }

    Given("인증 실패") {
        When("권한이 없는 경우") {
            val request = webTestClient
                .get()
                .uri(endpoint)

            Then("Response 401 UNAUTHORIZED") {
                request
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }

        When("권한이 customer 인경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken("customer"))
                .get().uri(endpoint)

            Then("Response 403 FORBIDDEN") {
                request
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        When("권한이 올바르지 않은 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken("unknown"))
                .get().uri(endpoint)

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
            .get()

        When("검색 결과가 존재하지 않을 경우") {
            Then("Response 200 OK - 비어있는 Flow 를 응답") {
                coEvery { orderQueryService.searchAll(any()) } returns emptyFlow()
                request
                    .uri { builder ->
                        builder
                            .queryParam("query", "test")
                            .path(endpoint)
                            .build()
                    }
                    .exchange()
                    .expectStatus().isOk

            }
        }

        When("isOk") {
            And("주문번호로 검색 요청된 경우") {
                Then("Response 200 OK - 응답 주문과 주문번호가 일치한다") {
                    val number = Instant.now().toEpochMilli().toString()
                    coEvery { orderQueryService.searchAll(any()) } returns flow {
                        order {
                            this.number = number
                            status = OrderStatus.PAID
                            payment = payment()
                        }
                    }

                    request
                        .uri { builder ->
                            builder
                                .queryParam("query", number)
                                .path(endpoint)
                                .build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBodyList<Order>()
                        .returnResult()
                        .responseBody!!
                        .should { orders ->
                            orders.forEach {
                                it.number shouldBe number
                            }
                        }
                }
            }

            And("휴대폰번호로 검색 요청된 경우") {
                Then("Response 200 OK - 응답 주문과 휴대폰번호가 일치한다") {
                    val phoneNumber = "01012341234"
                    coEvery { orderQueryService.searchAll(any()) } returns flow {
                        order {
                            status = OrderStatus.PLANNING
                            shippingInfo = shippingInfo {
                                primaryPhoneNumber = phoneNumber
                            }
                            payment = payment()
                            placedAt = Instant.now()
                            approvedAt = Instant.now().plus(Period.ofDays(1))
                        }
                    }

                    request
                        .uri { builder ->
                            builder
                                .queryParam("query", phoneNumber)
                                .path(endpoint)
                                .build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBodyList<Order>()
                        .returnResult()
                        .responseBody!!
                        .should { orders ->
                            orders.forEach {
                                it.shippingInfo!!.primaryPhoneNumber shouldBe phoneNumber
                            }
                        }
                }
            }

            And("수령인으로 검색 요청된 경우") {
                Then("Response 200 OK - 응답 주문과 수령인이 일치한다") {
                    val recipient = "홍길동"
                    coEvery { orderQueryService.searchAll(any()) } returns flow {
                        order {
                            status = OrderStatus.CANCELLED
                            shippingInfo = shippingInfo {
                                this.recipient = recipient
                            }
                            payment = payment()
                            refund = refund()
                            placedAt = Instant.now()
                            approvedAt = Instant.now().plus(Period.ofDays(1))
                        }
                    }

                    request
                        .uri { builder ->
                            builder
                                .queryParam("query", recipient)
                                .path(endpoint)
                                .build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBodyList<Order>()
                        .returnResult()
                        .responseBody!!
                        .should { orders ->
                            orders.forEach {
                                it.shippingInfo!!.recipient shouldBe recipient
                            }
                        }
                }
            }
        }
    }
})