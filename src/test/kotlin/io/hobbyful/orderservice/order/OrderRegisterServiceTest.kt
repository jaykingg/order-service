package io.hobbyful.orderservice.order

import com.ninjasquad.springmockk.MockkBean
import io.hobbyful.orderservice.cart.CartService
import io.hobbyful.orderservice.config.ValidatorConfig
import io.hobbyful.orderservice.fixtures.*
import io.hobbyful.orderservice.order.register.OrderRegisterService
import io.hobbyful.orderservice.order.register.variantIds
import io.hobbyful.orderservice.product.ProductService
import io.hobbyful.orderservice.variant.VariantService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import kotlinx.coroutines.flow.asFlow
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import

@DataMongoTest
@Import(
    ValidatorConfig::class,
    OrderRegisterService::class,
    OrderCrudService::class
)
class OrderRegisterServiceTest(
    @MockkBean private val cartService: CartService,
    @MockkBean private val productService: ProductService,
    @MockkBean private val variantService: VariantService,
    private val orderRegisterService: OrderRegisterService
) : DescribeSpec({
    describe("registerByCart(String): Order") {
        context("재고가 있는 경우") {
            val cart = cart()
            it("장바구니 정보로 새로운 주문 생성") {
                coEvery { cartService.getByCustomerId(cart.customerId) } returns cart
                val order = orderRegisterService.registerByCart(cart.customerId)
                order.number shouldContain """HF\d{13}""".toRegex()
            }
        }
    }

    describe("registerByPurchase(String, ItemsPayload) : Order") {
        val customerId = faker.random.nextUUID()
        val itemsPayload = itemPayloadFixture()
        context("재고가 있는 경우") {
            val productView = productView { id = itemsPayload.productId }
            val variantView = variantView { id = itemsPayload.items[0].variantId }
            val variantList = listOf(variantView)
            it("준비물 화면의 데이터로 새로운 주문 생성") {
                coEvery { productService.getViewById(productView.id) } returns productView
                coEvery {
                    variantService.getAllViewByIdsAndProductId(
                        itemsPayload.variantIds,
                        productView.id
                    )
                } returns variantList.asFlow()
                val order = orderRegisterService.registerByPurchase(customerId, itemsPayload)
                order.number shouldContain """HF\d{13}""".toRegex()
            }
        }
    }
})
