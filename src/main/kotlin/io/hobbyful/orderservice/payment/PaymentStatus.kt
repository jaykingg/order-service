package io.hobbyful.orderservice.payment

/**
 * 결제 처리 상태
 *
 * [코어 API > 결제](https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4)
 */
@Suppress("unused")
enum class PaymentStatus {
    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED
}
