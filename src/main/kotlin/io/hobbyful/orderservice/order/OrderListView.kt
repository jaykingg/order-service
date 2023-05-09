package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.payment.PaymentView
import org.bson.types.ObjectId

/**
 * 주문 리스트 View
 */
data class OrderListView(
    /**
     * 주문 ID
     */
    val id: ObjectId,

    /**
     * 주문번호
     */
    val number: String,

    /**
     * 주문이름
     */
    val name: String,

    /**
     * 주문상태
     */
    val status: OrderStatus,

    /**
     * 결제정보
     */
    val payment: PaymentView?
)
