package io.hobbyful.orderservice.eventStream.orderRefund

import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class OrderRefundSupplier(
    private val streamBridge: StreamBridge
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val BINDING = "order-refund"
    }

    fun send(payload: OrderRefundPayload) {
        try {
            val message = MessageBuilder.withPayload(payload)
                .setHeader("orderId", payload.orderId)
                .build()

            streamBridge.send(BINDING, message)
        } catch (exception: Exception) {
            log.error("$BINDING event 전송에 실패하였습니다", exception)
        }
    }
}
