package io.hobbyful.orderservice.payment

enum class TossWebhookEventType(val label: String) {
    PAYMENT_STATUS_CHANGED("결제 상태 변경 이벤트"),
    PAYOUT_STATUS_CHANGED("서브몰 지급대행 성공 또는 실패 이벤트"),
    METHOD_UPDATE("브랜드페이 고객 결제 수단 변경 이벤트"),
    CUSTOMER_STATUS_CHANGED("브랜드페이 고객 상태 변경 이벤트"),
    DEPOSIT_CALLBACK("가상계좌 입금 및 입금 취소 이벤트");
}