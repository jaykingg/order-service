package io.hobbyful.orderservice.eventStream.orderRefund

import io.hobbyful.orderservice.order.Order
import io.hobbyful.orderservice.storeCredit.StoreCredit
import org.bson.types.ObjectId

data class OrderRefundPayload(
    val orderId: ObjectId,
    val storeCredit: StoreCredit
) {
    companion object {
        fun from(order: Order) = OrderRefundPayload(
            orderId = order.id!!,
            storeCredit = order.storeCredit
        )

        fun of(orderId: ObjectId, storeCredit: StoreCredit) = OrderRefundPayload(
            orderId = orderId,
            storeCredit
        )
    }
}
