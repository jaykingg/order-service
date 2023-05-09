package io.hobbyful.orderservice.summary

/**
 * 배송 정책
 */
object ShippingPolicy {
    /**
     * 기본 배송비
     */
    const val BASE_COST = 3_000

    /**
     * 무료 배송을 위한 최소 주문금액
     */
    const val MIN_ORDER_AMOUNT = 50_000
}
