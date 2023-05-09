package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.tossPayments.ConfirmPaymentParameters
import java.time.Instant

/**
 * 결제 승인 request rarameter fixture
 */
inline fun confirmPaymentParameters(block: ConfirmPaymentParametersFixtureBuilder.() -> Unit = {}) =
    ConfirmPaymentParametersFixtureBuilder().apply(block).build()

class ConfirmPaymentParametersFixtureBuilder {
    var paymentKey: String = faker.random.randomString()
    var orderId: String = "HF${Instant.now().toEpochMilli()}"
    var amount: Int = faker.random.nextInt(1_000, 100_000)

    fun build() = ConfirmPaymentParameters(
        paymentKey = paymentKey,
        orderId = orderId,
        amount = amount
    )
}
