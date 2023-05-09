package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.core.PagedView
import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.payment
import io.hobbyful.orderservice.order.Order
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.refund.Refund
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
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
import java.time.Period
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class GetOrdersByAdminIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val endpoint = "/admin/orders"
    val customerId = UUID.randomUUID().toString()
    fun getOpaqueToken(authority: String = SecurityConstants.SERVICE_ADMIN) =
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(authority))
            .attributes { it["sub"] = customerId }

    Given("인증 실패") {
        When("권한이 없는 경우") {
            val request = webTestClient
                .get().uri(endpoint)

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

        When("ParameterObject 형식이 올바르지 않은 경우") {
            Then("Response 500 INTERNAL_SERVER_ERROR") {
                request
                    .uri(endpoint)
                    .exchange()
                    .expectStatus().is5xxServerError
            }

        }

        When("요청된 결제 기간 형식이 올바르지 않은 경우") {
            val placedAtRange = listOf("2022-01-01:00:00", "2023-01-01:00:00")

            Then("Response 500 INTERNAL_SERVER_ERROR") {
                request
                    .uri { uriBuilder ->
                        uriBuilder.path(endpoint)
                            .queryParam("placedAtRange", placedAtRange)
                            .queryParam("status", emptySet<OrderStatus>())
                            .build()
                    }
                    .exchange()
                    .expectStatus().is5xxServerError
            }
        }

        When("isOk") {
            val placedAtRange = listOf(Instant.now(), Instant.now().plus(Period.ofDays(1)))
            val orders = listOf(
                order {
                    number = Instant.now().minus(Period.ofDays(1)).toEpochMilli().toString()
                    status = OrderStatus.PAID
                    payment = payment()
                    placedAt = Instant.now().minus(Period.ofDays(1))
                },
                order {
                    number = Instant.now().plusSeconds(1).toEpochMilli().toString()
                    status = OrderStatus.CHECKOUT
                },
                order {
                    number = Instant.now().plusSeconds(2).toEpochMilli().toString()
                    status = OrderStatus.PAID
                    payment = payment()
                    placedAt = Instant.now().plusSeconds(2)
                },
                order {
                    number = Instant.now().plusSeconds(3).toEpochMilli().toString()
                    status = OrderStatus.CANCELLED
                    payment = payment()
                    refund = Refund("취소")
                    placedAt = Instant.now().plusSeconds(3)
                }
            )

            beforeEach {
                orderRepository.saveAll(orders).collect()
            }

            afterEach {
                orderRepository.deleteAll()
            }

            And("조회 주문상태가 비어있는 경우") {
                Then("모든 주문상태 조회 - Response 200 OK") {
                    request
                        .uri { uriBuilder ->
                            uriBuilder.path(endpoint)
                                .queryParam("placedAtRange", placedAtRange)
                                .queryParam("status", emptySet<OrderStatus>())
                                .build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<PagedView<Order>>()
                        .returnResult()
                        .shouldNotBeNull()
                        .responseBody!!
                        .should { view ->
                            view.content.shouldNotBeNull()
                            view.content.forAll { order ->
                                order.status.shouldBeTypeOf<OrderStatus>()
                                order.placedAt?.shouldBeGreaterThanOrEqualTo(placedAtRange[0])
                                order.placedAt?.shouldBeLessThanOrEqualTo(placedAtRange[1])
                                order.placedAt.shouldNotBeNull()
                                order.createdAt.shouldNotBeNull()
                                order.updatedAt.shouldNotBeNull()
                            }
                        }
                }
            }

            And("조회 주문상태가 존재하는 경우") {
                Then("요쳥된 주문상태만 조회 - Response 200 OK") {
                    val statusSet = setOf(OrderStatus.CHECKOUT, OrderStatus.PAID)
                    request
                        .uri { uriBuilder ->
                            uriBuilder.path(endpoint)
                                .queryParam("placedAtRange", placedAtRange)
                                .queryParam("status", setOf(OrderStatus.CHECKOUT, "paid"))
                                .build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<PagedView<Order>>()
                        .returnResult()
                        .shouldNotBeNull()
                        .responseBody!!
                        .should { view ->
                            view.content.shouldNotBeNull()
                            view.content.count() shouldBe 1
                            view.content.forAll { order ->
                                order.status shouldBeIn statusSet
                                order.placedAt?.shouldBeGreaterThanOrEqualTo(placedAtRange[0])
                                order.placedAt?.shouldBeLessThanOrEqualTo(placedAtRange[1])
                                order.placedAt.shouldNotBeNull()
                                order.createdAt.shouldNotBeNull()
                                order.updatedAt.shouldNotBeNull()
                            }
                        }
                }
            }
        }
    }
})
