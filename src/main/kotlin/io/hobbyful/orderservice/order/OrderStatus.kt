package io.hobbyful.orderservice.order

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 주문 상태
 */
enum class OrderStatus(val label: String) {
    /**
     * 주문대기
     */
    @field: JsonProperty("pending")
    PENDING("주문대기"),

    /**
     * 결제요청
     */
    @field: JsonProperty("checkout")
    CHECKOUT("결제요청"),

    /**
     * 결제완료
     */
    @field: JsonProperty("paid")
    PAID("결제완료"),

    /**
     * 배송준비중
     */
    @field: JsonProperty("planning")
    PLANNING("배송준비중"),

    /**
     * 배송중
     */
    @field: JsonProperty("shipping")
    SHIPPING("배송중"),

    /**
     * 배송완료
     */
    @field: JsonProperty("delivered")
    DELIVERED("배송완료"),

    /**
     * 주문확정
     */
    @field: JsonProperty("closed")
    CLOSED("주문확정"),

    /**
     * 취소완료
     */
    @field: JsonProperty("cancelled")
    CANCELLED("결제취소")
}
