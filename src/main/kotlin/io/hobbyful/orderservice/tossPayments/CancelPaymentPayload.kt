package io.hobbyful.orderservice.tossPayments

import io.hobbyful.orderservice.payment.Payment
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Positive

/**
 * 토스페이먼츠 > 결제 취소 호출을 위한 Request Body
 *
 * **참고**: [토스페이먼츠 API > 결제 취소](https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%B7%A8%EC%86%8C)
 */
data class CancelPaymentPayload(
    /**
     * 결제를 취소하는 이유입니다. 최대 길이는 200자 입니다.
     */
    @field: Length(min = 1, max = 200)
    val cancelReason: String,

    /**
     * 취소할 금액입니다. 값이 없으면 전액 취소됩니다.
     */
    @field: Positive
    val cancelAmount: Int? = null,

    /**
     * 현재 환불 가능한 금액입니다. 취소 요청을 안전하게 처리하기 위해서 사용합니다.
     */
    @field: Positive
    val refundableAmount: Int
) {
    companion object {
        fun of(reason: String, payment: Payment) = CancelPaymentPayload(
            cancelReason = reason,
            refundableAmount = payment.balanceAmount
        )
    }
}
