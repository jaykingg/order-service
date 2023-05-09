package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.lineitem.LineItemView
import io.hobbyful.orderservice.payment.PaymentView
import io.hobbyful.orderservice.shippingInfo.ShippingInfo
import io.hobbyful.orderservice.summary.Summary
import org.bson.types.ObjectId

/**
 * 주문 상세 View
 */
data class OrderView(
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
     * 주문 상품 리스트
     */
    val lineItems: List<LineItemView>,

    /**
     * 배송지
     */
    val shippingInfo: ShippingInfo?,

    /**
     * 결제내역
     */
    val payment: PaymentView?,

    /**
     * 주문정산 요약
     */
    val summary: Summary
)

