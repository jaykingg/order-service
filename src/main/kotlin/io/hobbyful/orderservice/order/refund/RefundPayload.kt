package io.hobbyful.orderservice.order.refund

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.NotBlank

/**
 * 주문 결제 취소 Request Payload
 */
data class RefundPayload(
    /**
     * 취소 유형
     */
    @field: NotBlank
    val reason: String,

    /**
     * 상세 내용
     */
    @field: Length(min = 1, max = 500)
    val message: String? = null
)

fun RefundPayload.toRefund() = Refund(
    reason = reason,
    message = message
)
