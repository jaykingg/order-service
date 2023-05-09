package io.hobbyful.orderservice.tossPayments

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

/**
 * 토스페이먼츠 > 결제 승인 Request Parameter
 *
 * **참고**: [토스페이먼츠 API > 결제 승인](https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8)
 */
data class ConfirmPaymentParameters(
    /**
     * 결제 건에 대한 고유한 키 값
     */
    @field: NotBlank
    val paymentKey: String,

    /**
     * 토스페이먼츠로 제공된 주문번호
     */
    @field: NotBlank
    val orderId: String,

    /**
     * 결제 금액
     */
    @field: Positive
    val amount: Int
)
