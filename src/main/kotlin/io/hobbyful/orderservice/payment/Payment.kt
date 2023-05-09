package io.hobbyful.orderservice.payment

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.hobbyful.orderservice.order.OnPaidStatus
import io.hobbyful.orderservice.order.OnRefundedStatus
import java.time.Instant
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * 토스페이먼츠의 결제 정의
 *
 * [코어 API > 결제](https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4)
 */
data class Payment(
    /**
     * Payment 객체의 응답 버전
     */
    val version: String,

    /**
     * 결제 건에 대한 고유한 키 값
     */
    val paymentKey: String,

    /**
     * 결제 건에 대한 주문번호
     */
    val orderId: String?,

    /**
     * 결제 수단
     */
    @field: NotNull(groups = [OnPaidStatus::class])
    val method: String?,

    /**
     * 총 결제 금액
     */
    val totalAmount: Int,

    /**
     * 취소할 수 있는 금액
     */
    val balanceAmount: Int,

    /**
     * 결제 처리 상태
     */
    val status: PaymentStatus,

    /**
     * 결제 승인 시간
     */
    @field: JsonDeserialize(using = IsoDateDeserializer::class)
    val approvedAt: Instant,

    /**
     * 결제 취소 이력
     */
    @field: NotEmpty(groups = [OnRefundedStatus::class])
    val cancels: List<Cancel>? = null,

    /**
     * 카드 정보
     */
    val card: CardInfo? = null,

    /**
     * 계좌이체 정보
     */
    val transfer: TransferInfo? = null,

    /**
     * 간편결제 정보
     */
    val easyPay: EasyPayInfo? = null
) {
    val cancelReason: String?
        get() = cancels?.sortedByDescending(Cancel::canceledAt)?.first()?.cancelReason
}
