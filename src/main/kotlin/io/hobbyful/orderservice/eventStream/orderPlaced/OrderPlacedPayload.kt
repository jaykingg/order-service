package io.hobbyful.orderservice.eventStream.orderPlaced

import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.order.Order
import org.bson.types.ObjectId
import javax.validation.constraints.NotBlank

data class OrderPlacedPayload(
    /**
     * 주문 ID
     */
    val orderId: ObjectId,

    /**
     * 고객 ID
     */
    @field: NotBlank
    val customerId: String,

    /**
     * 장바구니 ID
     */
    val cartId: ObjectId?,

    /**
     * 주문품목 리스트
     */
    @field: NotBlank
    val lineItems: List<LineItem>
) {
    companion object {
        fun from(order: Order) = OrderPlacedPayload(
            orderId = order.id!!,
            cartId = order.cartId,
            customerId = order.customerId,
            lineItems = order.lineItems
        )
    }
}