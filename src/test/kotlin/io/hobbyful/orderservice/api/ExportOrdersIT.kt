package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.fixtures.lineItem
import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.payment
import io.hobbyful.orderservice.fixtures.refund
import io.hobbyful.orderservice.order.OrderExport
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
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
import org.springframework.test.web.reactive.server.expectBodyList
import java.time.Instant
import java.time.Period
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class ExportOrdersIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository
) : BehaviorSpec({
    val endpoint = "/admin/orders/export"
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
        val request = webTestClient.mutateWith(getOpaqueToken()).get()

        When("Request Parameter 가 잘못되었을 경우") {
            And("결제 완료 기간이 없는 경우") {
                Then("Bad request") {
                    request
                        .uri {
                            it.queryParam("placedAtRange", "")
                            it.path(endpoint)
                            it.build()
                        }
                        .exchange()
                        .expectStatus().isBadRequest
                }
            }
        }

        When("isOk") {
            And("조회 기간에 해당하는 주문이 없을 경우") {
                beforeEach {
                    orderRepository.saveAll(OrderStatus.values().mapIndexed { index, orderStatus ->
                        order {
                            number = Instant.now().plusSeconds(index.toLong()).toEpochMilli().toString()
                            status = orderStatus
                            payment = paymentByStatus(orderStatus)
                            refund = refundByStatus(orderStatus)
                        }
                    }).collect()
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 200 OK - 빈 리스트 응답") {
                    request
                        .uri {
                            it.queryParam(
                                "placedAtRange",
                                listOf(Instant.now().plus(Period.ofDays(10)), Instant.now().plus(Period.ofDays(11)))
                            )
                            it.queryParam("status", listOf(OrderStatus.PAID, OrderStatus.CANCELLED))
                            it.path(endpoint)
                            it.build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBodyList<OrderExport>()
                        .returnResult()
                        .responseBody
                        .shouldBeEmpty()
                }
            }

            And("조회할 주문 상태가 비어 있는 경우") {
                beforeEach {
                    orderRepository.saveAll(OrderStatus.values().mapIndexed { index, orderStatus ->
                        order {
                            number = Instant.now().plusSeconds(index.toLong()).toEpochMilli().toString()
                            status = orderStatus
                            payment = paymentByStatus(orderStatus)
                            refund = refundByStatus(orderStatus)
                        }
                    }).collect()
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 200 OK - 모든 주문상태의 주문 응답") {
                    val searchRange =
                        listOf(Instant.now().minus(Period.ofDays(1)), Instant.now().plus(Period.ofDays(1)))

                    request
                        .uri {
                            it.queryParam("placedAtRange", searchRange)
                            it.path(endpoint)
                            it.build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBodyList<OrderExport>()
                        .returnResult()
                        .responseBody
                        .shouldNotBeNull()
                        .should {
                            val onPaidStatusSet = setOf(
                                OrderStatus.CANCELLED,
                                OrderStatus.CLOSED,
                                OrderStatus.DELIVERED,
                                OrderStatus.PAID,
                                OrderStatus.PLANNING,
                                OrderStatus.SHIPPING
                            )

                            it.size shouldBe onPaidStatusSet.size
                            it.forEach { orderExport ->
                                orderExport.placedAt!! shouldBeGreaterThanOrEqualTo searchRange[0]
                                orderExport.placedAt!! shouldBeLessThanOrEqualTo searchRange[1]
                                orderExport.status shouldBeIn onPaidStatusSet
                            }
                        }
                }
            }

            And("특정 주문 상태로 조회할 경우") {
                val allStatusList = OrderStatus.values().toList()
                val orderList = listOf(
                    order {
                        number = Instant.now().plusSeconds(allStatusList.indexOf(OrderStatus.CHECKOUT).toLong())
                            .toEpochMilli().toString()
                        status = OrderStatus.CHECKOUT
                        lineItems = List(allStatusList.indexOf(OrderStatus.CHECKOUT)) { lineItem() }
                    },
                    order {
                        number =
                            Instant.now().plusSeconds(allStatusList.indexOf(OrderStatus.PAID).toLong()).toEpochMilli()
                                .toString()
                        status = OrderStatus.PAID
                        lineItems = List(allStatusList.indexOf(OrderStatus.PAID)) { lineItem() }
                        payment = payment {
                            method = "카드"
                            easyPay = null
                        }

                    },
                    order {
                        number = Instant.now().plus(Period.ofDays(100 + allStatusList.indexOf(OrderStatus.PAID)))
                            .toEpochMilli().toString()
                        status = OrderStatus.PAID
                        lineItems = List(allStatusList.indexOf(OrderStatus.PAID)) { lineItem() }
                        payment = payment {
                            method = "카드"
                            easyPay = null
                        }
                        placedAt = Instant.now().plus(Period.ofDays(100))
                    },
                    order {
                        number = Instant.now().plusSeconds(allStatusList.indexOf(OrderStatus.PLANNING).toLong())
                            .toEpochMilli().toString()
                        status = OrderStatus.PLANNING
                        lineItems = List(allStatusList.indexOf(OrderStatus.PLANNING)) { lineItem() }
                        payment = payment {
                            method = "간편결제"
                            card = null
                        }
                    },
                    order {
                        number = Instant.now().plus(Period.ofDays(100 + allStatusList.indexOf(OrderStatus.PLANNING)))
                            .toEpochMilli().toString()
                        status = OrderStatus.PLANNING
                        lineItems = List(allStatusList.indexOf(OrderStatus.PLANNING)) { lineItem() }
                        payment = payment {
                            method = "간편결제"
                            card = null
                        }
                        placedAt = Instant.now().plus(Period.ofDays(100))
                    },
                    order {
                        number = Instant.now().plusSeconds(allStatusList.indexOf(OrderStatus.CANCELLED).toLong())
                            .toEpochMilli().toString()
                        status = OrderStatus.CANCELLED
                        lineItems = List(allStatusList.indexOf(OrderStatus.CANCELLED)) { lineItem() }
                        payment = payment()
                        refund = refund()
                    },
                    order {
                        number = Instant.now().plus(Period.ofDays(100 + allStatusList.indexOf(OrderStatus.CANCELLED)))
                            .toEpochMilli().toString()
                        status = OrderStatus.CANCELLED
                        lineItems = List(allStatusList.indexOf(OrderStatus.CANCELLED)) { lineItem() }
                        payment = payment()
                        refund = refund()
                        placedAt = Instant.now().plus(Period.ofDays(100))
                    },
                    order {
                        number = Instant.now().plusSeconds(allStatusList.indexOf(OrderStatus.SHIPPING).toLong())
                            .toEpochMilli().toString()
                        status = OrderStatus.SHIPPING
                        lineItems = List(allStatusList.indexOf(OrderStatus.SHIPPING)) { lineItem() }
                        payment = payment()
                    },
                    order {
                        number = Instant.now().plusSeconds(allStatusList.indexOf(OrderStatus.DELIVERED).toLong())
                            .toEpochMilli().toString()
                        status = OrderStatus.DELIVERED
                        lineItems = List(allStatusList.indexOf(OrderStatus.DELIVERED)) { lineItem() }
                        payment = payment()
                    },
                    order {
                        number =
                            Instant.now().plusSeconds(allStatusList.indexOf(OrderStatus.CLOSED).toLong()).toEpochMilli()
                                .toString()
                        status = OrderStatus.CLOSED
                        lineItems = List(allStatusList.indexOf(OrderStatus.CLOSED)) { lineItem() }
                        payment = payment()
                    }
                )

                beforeEach {
                    orderRepository.saveAll(orderList).collect()
                }

                afterEach {
                    orderRepository.deleteAll()
                }

                Then("Response 200 OK - 조건에 맞는 주문 응답") {
                    val searchRange =
                        listOf(Instant.now().minus(Period.ofDays(1)), Instant.now().plus(Period.ofDays(1)))
                    val searchStatus = listOf(OrderStatus.PAID, OrderStatus.PLANNING, OrderStatus.CANCELLED)

                    request
                        .uri {
                            it.queryParam("placedAtRange", searchRange)
                            it.queryParam("status", searchStatus)
                            it.path(endpoint)
                            it.build()
                        }
                        .exchange()
                        .expectStatus().isOk
                        .expectBodyList<OrderExport>()
                        .returnResult()
                        .responseBody
                        .shouldNotBeNull()
                        .should {
                            var resultSize = 0
                            searchStatus.forEach { status ->
                                resultSize += allStatusList.indexOf(status)
                            }

                            it.size shouldBe resultSize
                            it.forEach { orderExport ->
                                orderExport.placedAt!! shouldBeGreaterThanOrEqualTo searchRange[0]
                                orderExport.placedAt!! shouldBeLessThanOrEqualTo searchRange[1]
                                orderExport.status shouldBeIn searchStatus
                                if (orderExport.cardCompany == null) orderExport.easyPayProvider.shouldNotBeNull()
                                if (orderExport.easyPayProvider == null) orderExport.cardCompany.shouldNotBeNull()
                            }
                        }
                }
            }
        }
    }
})