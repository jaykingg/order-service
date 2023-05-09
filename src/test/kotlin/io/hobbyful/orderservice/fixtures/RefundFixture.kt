package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.order.refund.Refund
import java.time.Instant

inline fun refund(block: RefundFixture.() -> Unit = {}) = RefundFixture().apply(block).build()

class RefundFixture {
    var reason: String = "test-refund-reason"
    var message: String? = "test-refund-message"
    var approvedAt: Instant? = Instant.now()

    fun build() = Refund(
        reason = reason,
        message = message,
        approvedAt = approvedAt
    )
}