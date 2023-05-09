package io.hobbyful.orderservice.payment

/**
 * 간편결제 정보
 */
data class EasyPayInfo(
    /**
     * 결제 금액
     */
    val amount: Int,

    /**
     * 사용한 간편결제 수단
     */
    val provider: String
)
