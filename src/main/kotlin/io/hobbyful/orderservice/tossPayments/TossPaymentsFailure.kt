package io.hobbyful.orderservice.tossPayments

/**
 * 토스페이먼츠 결제 실패 정보
 */
data class TossPaymentsFailure(
    /**
     * 에러 코드
     */
    val code: String,

    /**
     * 에러 메세지
     */
    val message: String
)
