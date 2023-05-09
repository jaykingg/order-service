package io.hobbyful.orderservice.eventStream.orderClosed

import io.hobbyful.orderservice.order.OrderUpdateView
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Component

@Component
class OrderClosedSupplier(
    private val streamBridge: StreamBridge
) {
    companion object {
        const val BINDING = "order-closed"
    }

    fun send(result: OrderUpdateView) {
        streamBridge.send(BINDING, result)
    }
}
