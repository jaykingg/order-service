package io.hobbyful.orderservice.order.refund

import io.hobbyful.orderservice.core.ErrorCodeException
import io.hobbyful.orderservice.core.InternalServerException
import io.hobbyful.orderservice.eventStream.orderRefund.OrderRefundPayload
import io.hobbyful.orderservice.eventStream.orderRefund.OrderRefundSupplier
import io.hobbyful.orderservice.order.*
import io.hobbyful.orderservice.payment.Payment
import io.hobbyful.orderservice.tossPayments.CancelPaymentPayload
import io.hobbyful.orderservice.tossPayments.TossPaymentsFailure
import io.hobbyful.orderservice.tossPayments.TossPaymentsTemplate
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.awaitBody

@Service
class OrderRefundService(
    private val orderCrudService: OrderCrudService,
    private val orderRefundSupplier: OrderRefundSupplier,
    private val tossPaymentsTemplate: TossPaymentsTemplate,
) {
    /**
     * 주문의 결제를 취소하고 `결제취소` 상태로 변경합니다.
     *
     * @param orderId 주문 Id
     * @param customerId 고객 Id
     * @param payload 주문 결제 취소 Request Payload
     */
    suspend fun refund(orderId: ObjectId, customerId: String, payload: RefundPayload) {
        val order = orderCrudService.getByIdAndCustomerId(orderId, customerId)

        if (order.isCancelledStatus) {
            throw ErrorCodeException.of(OrderError.ORDER_ALREADY_REFUNDED)
        }
        if (order.isNotPaidStatus || order.payment == null) {
            throw ErrorCodeException.of(OrderError.ORDER_NOT_REFUNDABLE)
        }

        orderCrudService
            .save(
                order.copy(
                    status = OrderStatus.CANCELLED,
                    payment = requestCancelPayment(order.payment, payload.reason),
                    refund = payload.toRefund()
                )
            )
            .also { orderRefundSupplier.send(OrderRefundPayload.from(it)) }
    }

    private suspend fun requestCancelPayment(payment: Payment, reason: String): Payment =
        tossPaymentsTemplate.cancelPayment(payment.paymentKey, CancelPaymentPayload.of(reason, payment)) { response ->
            if (response.statusCode().is4xxClientError) {
                response.awaitBody<TossPaymentsFailure>().run {
                    throw ErrorCodeException(message, code)
                }
            }
            if (response.statusCode().is5xxServerError) throw InternalServerException()
            response.awaitBody()
        }
}
