package io.hobbyful.orderservice.payment

import io.hobbyful.orderservice.core.ErrorCodeException
import io.hobbyful.orderservice.core.ErrorResponse
import io.hobbyful.orderservice.eventStream.orderPlaced.OrderPlacedSupplier
import io.hobbyful.orderservice.eventStream.orderRefund.OrderRefundPayload
import io.hobbyful.orderservice.eventStream.orderRefund.OrderRefundSupplier
import io.hobbyful.orderservice.order.*
import io.hobbyful.orderservice.order.refund.Refund
import io.hobbyful.orderservice.storeCredit.ChargeStoreCreditPayload
import io.hobbyful.orderservice.storeCredit.StoreCreditChargeTemplate
import io.hobbyful.orderservice.tossPayments.ConfirmPaymentParameters
import io.hobbyful.orderservice.tossPayments.TossPaymentsFailure
import io.hobbyful.orderservice.tossPayments.TossPaymentsTemplate
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.server.ResponseStatusException

/**
 * 주문 결제 서비스
 */
@Service
class PaymentService(
    private val orderCrudService: OrderCrudService,
    private val orderPlacedSupplier: OrderPlacedSupplier,
    private val orderRefundSupplier: OrderRefundSupplier,
    private val storeCreditChargeTemplate: StoreCreditChargeTemplate,
    private val tossPaymentsTemplate: TossPaymentsTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    suspend fun confirmPayment(parameters: ConfirmPaymentParameters): Order =
        orderCrudService.getByNumber(parameters.orderId).let { order ->
            if (order.isNotCheckoutStatus) throw ErrorCodeException.of(OrderError.INVALID_ORDER_TO_CHECKOUT)
            if (order.total != parameters.amount) throw ErrorCodeException.of(PaymentError.INVALID_PAYMENT_AMOUNT)

            val storeCredit = order.storeCredit.let {
                if (it.amount > 0) it.copy(transaction = requestChargeStoreCredit(order))
                else it
            }

            val payment = requestConfirmPayment(
                parameters,
                OrderRefundPayload.of(order.id!!, storeCredit)
            )

            orderCrudService
                .save(
                    order.copy(
                        storeCredit = storeCredit,
                        payment = payment,
                        status = OrderStatus.PAID,
                        placedAt = payment.approvedAt
                    )
                )
                .also(orderPlacedSupplier::send)
        }

    private suspend fun requestChargeStoreCredit(order: Order) =
        chargeStoreCreditPayload(order).let { payload ->
            storeCreditChargeTemplate.charge(order.customerId, payload) {
                if (it.statusCode().is4xxClientError) {
                    throw ErrorCodeException.of(it.awaitBody<ErrorResponse>())
                }
                if (it.statusCode().is5xxServerError) {
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
                }

                it.awaitBody()
            }
        }

    private suspend fun requestConfirmPayment(
        parameters: ConfirmPaymentParameters,
        payload: OrderRefundPayload
    ): Payment =
        tossPaymentsTemplate.confirmPayment(parameters) {
            if (it.statusCode().isError) {
                orderRefundSupplier.send(payload)
            }
            if (it.statusCode().is4xxClientError) {
                val error = it.awaitBody<TossPaymentsFailure>()
                throw ErrorCodeException(error.message, error.code)
            }
            if (it.statusCode().is5xxServerError) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
            }

            it.awaitBody()
        }

    private fun chargeStoreCreditPayload(order: Order) = ChargeStoreCreditPayload(
        orderId = order.id!!,
        amount = order.storeCredit.amount
    )

    suspend fun cancelByWebhook(payload: TossWebhookPayload) {
        if (!payload.isPaymentCancelled) return
        if (payload.data.orderId.isNullOrBlank() || payload.data.cancels.isNullOrEmpty()) {
            throw ErrorCodeException.of(PaymentError.INVALID_WEBHOOK_PAYLOAD)
        }

        orderCrudService.getByNumber(payload.data.orderId).let { order ->
            if (order.isCancelledStatus) return logger.error(OrderError.ORDER_ALREADY_REFUNDED.message)

            orderCrudService
                .save(
                    order.copy(
                        status = OrderStatus.CANCELLED,
                        payment = payload.data,
                        refund = Refund.of(
                            payload.data.cancelReason.orEmpty().ifBlank { "관리자 취소" }
                        )
                    )
                )
                .also { orderRefundSupplier.send(OrderRefundPayload.from(it)) }
        }
    }
}
