package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.payment.PaymentView
import java.time.Instant

inline fun paymentView(block: PaymentViewFixture.() -> Unit = {}) = PaymentViewFixture().apply(block).build()

class PaymentViewFixture {
    var totalAmount: Int = faker.random.nextInt(10_000, 50_000)
    var approvedAt: Instant = Instant.now()
    var method = "간편결제"
    var card = "국민"
    var easyPay = "카카오페이"
    var bank = "카카오"

    fun build() = PaymentView(
        totalAmount = totalAmount,
        approvedAt = approvedAt,
        method = method,
        card = card,
        easyPay = easyPay,
        bank = bank
    )
}
