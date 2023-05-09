package io.hobbyful.orderservice.order.register

import io.hobbyful.orderservice.cart.Cart
import io.hobbyful.orderservice.cart.CartService
import io.hobbyful.orderservice.cart.pickedLineItems
import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.order.Order
import io.hobbyful.orderservice.order.Order.Companion.order
import io.hobbyful.orderservice.order.OrderCrudService
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.StoreType
import io.hobbyful.orderservice.product.ProductService
import io.hobbyful.orderservice.variant.VariantService
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.zip
import org.springframework.stereotype.Service
import javax.validation.ConstraintViolationException
import javax.validation.Validator

/**
 * 주문 등록 서비스
 */
@Service
class OrderRegisterService(
    private val cartService: CartService,
    private val productService: ProductService,
    private val variantService: VariantService,
    private val orderCrudService: OrderCrudService,
    private val validator: Validator
) {
    /**
     * 고객의 장바구니 데이터로 새로운 주문 생성
     *
     * @param customerId 고객 ID
     */
    suspend fun registerByCart(customerId: String): Order =
        cartService.getByCustomerId(customerId).let { cart ->
            validateForRegister(cart)
            orderCrudService.save(
                order {
                    this.customerId = customerId
                    this.cartId = cart.id
                    store = StoreType.HOBBYFUL
                    status = OrderStatus.PENDING
                    lineItems = cart.pickedLineItems
                }
            )
        }

    /**
     * 준비물 화면에서 선택된 데이터로 새로운 주문 생성
     */
    suspend fun registerByPurchase(customerId: String, payload: ItemsPayload): Order {
        val productView = productService.getViewById(payload.productId)
        val variantsView = variantService.getAllViewByIdsAndProductId(payload.variantIds, productView.id)
        val items = payload.items.asFlow()

        val lineItems = variantsView.zip(items) { variant, item ->
            LineItem.of(
                product = productView,
                variant = variant,
                quantity = item.quantity,
                picked = true
            )
        }

        return orderCrudService.save(
            order {
                this.customerId = customerId
                store = StoreType.HOBBYFUL
                status = OrderStatus.PENDING
                this.lineItems = lineItems.toList()
            }
        )
    }

    /**
     * 주문 등록을 위해 조회된 장바구니 유효성 체크
     *
     * @param cart 하비풀 장바구니
     */
    private fun validateForRegister(cart: Cart) {
        val violations = validator.validate(cart)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException("주문을 생성할 수 없는 장바구니입니다", violations)
        }
    }
}
