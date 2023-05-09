package io.hobbyful.orderservice.payment

import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

/**
 * 결제 상세 View
 */
data class PaymentView(
    /**
     * 결제 총액
     */
    val totalAmount: Int,

    /**
     * 결제 승인 시간
     */
    val approvedAt: Instant,

    /**
     * 결제 수단
     */
    val method: String,

    /**
     * 카드결제 또는 간편결제에 사용된 카드사 코드
     */
    @Field("card.company")
    val card: String?,

    /**
     * 간편결제 수단
     */
    @Field("easyPay.provider")
    val easyPay: String?,

    /**
     * 간편결제를 통한 계좌이체에 사용된 은행 코드
     */
    @Field("transfer.bank")
    val bank: String?
)
