package io.hobbyful.orderservice.order

data class AdminStatusView(
    /**
     * 주문상태 변경에 실패한 주문번호 리스트
     */
    val failedNumbers: Set<String>
)