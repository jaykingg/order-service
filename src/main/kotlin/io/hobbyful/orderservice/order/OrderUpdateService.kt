package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.core.ErrorCodeException
import io.hobbyful.orderservice.eventStream.orderClosed.OrderClosedSupplier
import org.springframework.stereotype.Service

@Service
class OrderUpdateService(
    private val orderRepository: OrderRepository,
    private val orderClosedSupplier: OrderClosedSupplier
) {
    suspend fun approveAllPaidOrders() = OrderUpdateView(
        orderRepository.approveAllPaid()
    )

    suspend fun closeAllDeliveredOrders() =
        OrderUpdateView(orderRepository.closeAllDelivered())
            .also { orderClosedSupplier.send(it) }

    suspend fun changeOrderStatus(payload: AdminStatusPayload, targetStatus: OrderStatus): OrderUpdateView =
        OrderUpdateView(
            totalCount = when (targetStatus) {
                OrderStatus.SHIPPING -> orderRepository.setShippingByNumbers(payload.numbers)
                OrderStatus.DELIVERED -> orderRepository.setDeliveredByNumbers(payload.numbers)
                else -> throw ErrorCodeException.of(AdminError.INVALID_TARGET_STATUS_TO_CHANGE)
            }
        )
}
