package io.hobbyful.orderservice.cart

import com.ninjasquad.springmockk.MockkBean
import io.hobbyful.orderservice.core.NotFoundException
import io.hobbyful.orderservice.fixtures.cart
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import org.springframework.context.annotation.Import
import java.util.*

@Import(CartService::class)
class CartServiceTest(
    @MockkBean private val cartRepository: CartRepository,
    private val cartService: CartService
) : DescribeSpec({
    val customerId: String = UUID.randomUUID().toString()

    afterTest {
        clearAllMocks()
    }

    describe("getByCustomerId(customerId: String): Cart") {
        context("고객의 ID로 장바구니가 존재하지 않을때") {
            it("throw NotFoundException") {
                coEvery { cartRepository.findByCustomerIdToCheckout(customerId) } returns null
                shouldThrowExactly<NotFoundException> {
                    cartService.getByCustomerId(customerId)
                }
            }
        }

        context("고객의 ID로 장바구니가 존재할때") {
            val cart = cart { this.customerId = customerId }

            it("해당 고객의 장바구니 리턴") {
                coEvery { cartRepository.findByCustomerIdToCheckout(customerId) } returns cart
                cartService.getByCustomerId(customerId) shouldBe cart
            }
        }
    }
})
