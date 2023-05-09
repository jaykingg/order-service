package io.hobbyful.orderservice.payment

import io.hobbyful.orderservice.core.BaseError

/**
 * 결제 에러 정의
 */
enum class PaymentError(override val message: String) : BaseError {
    /**
     * 주문금액과 결제금액이 일치하지 않는 경우
     */
    INVALID_PAYMENT_AMOUNT("결제금액이 일치하지 않습니다"),

    /**
     * 토스페이먼츠에서 전송한 Webhook Payload가 유효하지 않은 경우
     */
    INVALID_WEBHOOK_PAYLOAD("처리할 수없는 웹훅 payload 입니다"),

    /**
     * 토스페이먼츠 배송중주문 취소 Webhook 이벤트 타입이 아닌 경우
     */
    INVALID_PAYMENT_EVENT_TYPE("토스페이먼츠 이벤트 타입 또는 결제상태가 올바르지 않습니다");
}
