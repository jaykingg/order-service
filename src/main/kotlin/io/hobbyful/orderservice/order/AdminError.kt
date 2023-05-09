package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.core.BaseError

enum class AdminError(override val message: String) : BaseError {
    INVALID_TARGET_STATUS_TO_CHANGE("변경할 수 없는 주문상태입니다"),
    EMPTY_NUMBERS_TO_CHANGE_STATUS("주문상태를 변경하기 위한 리스트가 빈 값입니다"),
}
