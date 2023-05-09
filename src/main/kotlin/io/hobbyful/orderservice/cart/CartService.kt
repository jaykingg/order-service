package io.hobbyful.orderservice.cart

import io.hobbyful.orderservice.core.NotFoundException
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartRepository: CartRepository
) {
    /**
     * 고객의 장바구니 조회
     *
     * @param customerId 고객 ID
     */
    suspend fun getByCustomerId(customerId: String): Cart =
        cartRepository.findByCustomerIdToCheckout(customerId) ?: throw NotFoundException()
}
