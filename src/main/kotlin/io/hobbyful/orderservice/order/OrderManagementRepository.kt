package io.hobbyful.orderservice.order

/**
 * 주문 관리에 필요한 업데이트 `methods`를 정의합니다.
 */
interface OrderManagementRepository {
    /**
     * 결제완료 상태의 모든 주문의 상태를 배송준비중으로 변경합니다.
     *
     * @return 마감된 주문의 개수
     */
    suspend fun approveAllPaid(): Long

    suspend fun closeAllDelivered(): Long

    suspend fun setShippingByNumbers(numbers: Collection<String>): Long

    suspend fun setDeliveredByNumbers(numbers: Collection<String>): Long
}
