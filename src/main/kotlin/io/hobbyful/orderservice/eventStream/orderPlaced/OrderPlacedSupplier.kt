package io.hobbyful.orderservice.eventStream.orderPlaced

import io.hobbyful.orderservice.order.Order
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class OrderPlacedSupplier(
    private val streamBridge: StreamBridge
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val BINDING = "order-placed"
    }

    fun send(order: Order) {
        try {
            val payload = MessageBuilder.withPayload(OrderPlacedPayload.from(order))
                .setHeader("orderId", order.id)
                .build()

            streamBridge.send(BINDING, payload)
        } catch (e: Exception) {
            log.error("$BINDING event 전송에 실패하였습니다", e)
        }
    }
}
