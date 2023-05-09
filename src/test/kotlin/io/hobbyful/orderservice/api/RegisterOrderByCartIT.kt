package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.cart.CartRepository
import io.hobbyful.orderservice.fixtures.cart
import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.order.OrderView
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
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
class RegisterOrderByCartIT(
    private val webTestClient: WebTestClient,
    private val cartRepository: CartRepository
) : BehaviorSpec({
    val tokenSubject = UUID.randomUUID().toString()
    val opaqueToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority("customer"))
        .attributes { it["sub"] = tokenSubject }

    Given("POST /orders - 장바구니 기준 새로운 주문 생성") {
        val request = webTestClient
            .mutateWith(opaqueToken)
            .post().uri("/orders")

        When("customerId의 장바구니(cart)가 존재하지 않을 때") {
            Then("Response 404 NOT_FOUND") {
                request
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("customerId의 장바구니(cart)가 존재할 때") {
            val cart = cart {
                customerId = tokenSubject
            }

            beforeEach {
                cartRepository.save(cart)
            }

            afterEach { cartRepository.deleteAll() }

            Then("200 ok") {
                request
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<OrderView>()
                    .returnResult()
                    .responseBody
                    .shouldNotBeNull()
                    .should {
                        it.summary.subtotal shouldBe cart.lineItems.sumOf(LineItem::total)
                        it.lineItems.first().total shouldBe it.lineItems.first().variant.price * it.lineItems.first().quantity
                    }
            }
        }
    }
})
