package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.order.refund.RefundPayload

inline fun refundPayload(block: RefundPayloadFixture.() -> Unit = {}) = RefundPayloadFixture().apply(block).build()

class RefundPayloadFixture {
    var reason: String = "test-refundPayload-reason"
    var message: String? = "test-refundPayload-message"

    fun build() = RefundPayload(
        reason = reason,
        message = message
    )
}