package io.hobbyful.orderservice.order.checkout

import io.hobbyful.orderservice.shippingInfo.ShippingInfo
import javax.validation.Valid
import javax.validation.constraints.PositiveOrZero

/**
 * 주문 결제요청 Request Payload
 */
data class CheckoutPayload(
    /**
     * 배송정보
     */
    @field: Valid
    val shippingInfo: ShippingInfo,

    /**
     * 주문에 차감할 고객의 적립금 액수
     */
    @field: PositiveOrZero
    val storeCreditAmount: Int = 0
)
