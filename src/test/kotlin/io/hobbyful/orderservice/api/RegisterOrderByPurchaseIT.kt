package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.fixtures.brand
import io.hobbyful.orderservice.fixtures.itemPayloadFixture
import io.hobbyful.orderservice.fixtures.product
import io.hobbyful.orderservice.fixtures.variant
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderView
import io.hobbyful.orderservice.product.ProductRepository
import io.hobbyful.orderservice.product.ProductView
import io.hobbyful.orderservice.product.brand.toProductBrand
import io.hobbyful.orderservice.variant.VariantRepository
import io.hobbyful.orderservice.variant.VariantView
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeTypeOf
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
class RegisterOrderByPurchaseIT(
    private val webTestClient: WebTestClient,
    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository,
    private val orderRepository: OrderRepository,
) : BehaviorSpec({
    val tokenSubject = UUID.randomUUID().toString()
    val opaqueToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority("customer"))
        .attributes { it["sub"] = tokenSubject }

    Given("POST /orders/buy-now - 바로 구매하기 기반 주문 생성") {
        val payload = itemPayloadFixture()
        val request = webTestClient
            .mutateWith(opaqueToken)
            .post().uri("/orders/buy-now")

        When("productId로 준비물이 존재하지 않을 때") {
            Then("throw isNotFound") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("productId & variantId가 존재하지 않을 때") {
            val objectId = ObjectId.get()

            beforeEach {
                productRepository.save(
                    product {
                        id = objectId
                    }
                )
            }

            afterEach { productRepository.deleteAll() }

            Then("throw isNotFound") {
                request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("productId & variantId가 존재할 때") {
            val productId = payload.productId
            val variantId = payload.items[0].variantId
            val brand = brand().toProductBrand()

            beforeEach {
                productRepository.save(product {
                    id = productId
                    this.brand = brand
                })
                variantRepository.save(variant {
                    id = variantId
                    this.brandId = brand.id
                    this.productId = productId
                })
            }

            afterEach {
                productRepository.deleteAll()
                variantRepository.deleteAll()
            }

            Then("200 ok") {
                val result = request
                    .bodyValue(payload)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<OrderView>()
                    .returnResult()
                    .responseBody!!

                result.lineItems[0].product.shouldBeTypeOf<ProductView>()
                result.lineItems[0].variant.shouldBeTypeOf<VariantView>()

                val order = orderRepository.findByIdAndCustomerId(result.id, tokenSubject)
                order!!.cartId.shouldBeNull()

            }
        }

    }
})
