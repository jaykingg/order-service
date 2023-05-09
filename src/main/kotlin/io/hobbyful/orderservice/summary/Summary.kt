package io.hobbyful.orderservice.summary

import javax.validation.constraints.PositiveOrZero

/**
 * 주문 결제 요약
 */
data class Summary(
    /**
     * 주문품목 총액
     */
    @field: PositiveOrZero
    val subtotal: Int,

    /**
     * 적립금 사용금액
     */
    @field: PositiveOrZero
    val storeCreditAmount: Int,

    /**
     * 고객 주문 쿠폰 할인금액
     */
    @field: PositiveOrZero
    val couponAmount: Int,

    /**
     * 배송비
     */
    @field: PositiveOrZero
    val shippingCost: Int,

    /**
     * 결제 총액
     */
    @field: PositiveOrZero
    val total: Int
) {
    companion object {
        /**
         * 주문 결제요약 생성
         */
        inline fun summary(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var subtotal: Int = 0
        var storeCreditAmount: Int = 0

        private val shippingCost
            get() = if (subtotal >= ShippingPolicy.MIN_ORDER_AMOUNT) 0 else ShippingPolicy.BASE_COST

        private val total
            get() = subtotal + shippingCost - storeCreditAmount

        fun build() = Summary(
            subtotal = subtotal,
            storeCreditAmount = storeCreditAmount,
            couponAmount = 0,
            shippingCost = shippingCost,
            total = total
        )
    }
}
