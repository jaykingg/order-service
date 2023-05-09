package io.hobbyful.orderservice.order.checkout

import io.hobbyful.orderservice.core.ErrorCodeException
import io.hobbyful.orderservice.lineitem.subtotal
import io.hobbyful.orderservice.order.*
import io.hobbyful.orderservice.storeCredit.StoreCredit
import io.hobbyful.orderservice.summary.Summary.Companion.summary
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCheckoutService(
    private val orderCrudService: OrderCrudService
) {
    /**
     * 주문의 결제요청 처리 및 상태 변경
     *
     * @param orderId 주문 ID
     * @param customerId 고객 ID
     * @param payload 결제요청 Request Payload
     */
    @Transactional
    suspend fun checkout(orderId: ObjectId, customerId: String, payload: CheckoutPayload): Order =
        orderCrudService.getByIdAndCustomerId(orderId, customerId).let { order ->
            if (order.isNotPendingStatus) {
                throw ErrorCodeException.of(OrderError.ORDER_NOT_PENDING)
            }
            if (order.total < payload.storeCreditAmount) {
                throw ErrorCodeException.of(OrderError.STORE_CREDIT_EXCEEDS_ORDER_TOTAL)
            }

            StoreCredit.of(payload.storeCreditAmount).let { storeCredit ->
                orderCrudService.save(
                    order.copy(
                        shippingInfo = payload.shippingInfo,
                        status = OrderStatus.CHECKOUT,
                        storeCredit = storeCredit,
                        summary = summary {
                            subtotal = order.lineItems.subtotal
                            storeCreditAmount = storeCredit.amount
                        }
                    )
                )
            }
        }
}
