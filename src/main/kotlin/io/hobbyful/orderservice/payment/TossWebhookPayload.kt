package io.hobbyful.orderservice.payment

/**
 * 토스페이먼츠 Webhook 정의
 *
 * [Webhook guide](https://docs.tosspayments.com/guides/webhook)
 */
data class TossWebhookPayload(
    /**
     * 토스페이먼츠 이벤트 Event Type
     */
    val eventType: TossWebhookEventType,

    /**
     * 토스페이먼츠 응답 객체
     */
    val data: Payment
) {
    val isPaymentCancelled: Boolean
        get() = eventType == TossWebhookEventType.PAYMENT_STATUS_CHANGED && data.status == PaymentStatus.CANCELED
}
