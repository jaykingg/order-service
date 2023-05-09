package io.hobbyful.orderservice.order

import javax.validation.constraints.NotEmpty

data class AdminStatusPayload(
    /**
     * 주문상태를 변경할 주문번호 리스트
     */
    @field: NotEmpty
    val numbers: Set<String>
)
