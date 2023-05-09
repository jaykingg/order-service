package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.core.BaseError

enum class OrderError(override val message: String) : BaseError {
    INVALID_ORDER_TO_CHECKOUT("결제할 수 없는 주문입니다"),

    ORDER_ALREADY_REFUNDED("이미 취소된 주문입니다"),

    ORDER_NOT_REFUNDABLE("취소할 수 없는 주문입니다"),

    ORDER_NOT_PENDING("주문대기 상태가 아닙니다"),

    STORE_CREDIT_EXCEEDS_ORDER_TOTAL("사용할 적립금이 주문 총액보다 많습니다");
}
