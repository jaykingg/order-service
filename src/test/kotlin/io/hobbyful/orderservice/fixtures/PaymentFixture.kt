package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.payment.CardInfo
import io.hobbyful.orderservice.payment.EasyPayInfo
import io.hobbyful.orderservice.payment.Payment
import io.hobbyful.orderservice.payment.PaymentStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

inline fun payment(block: PaymentFixtureBuilder.() -> Unit = {}) = PaymentFixtureBuilder().apply(block).build()

class PaymentFixtureBuilder {
    private val formatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())

    private val version = formatter.format(Instant.now())
    var paymentKey = faker.random.randomString()
    var orderId: String? = "HF${Instant.now().toEpochMilli()}"
    var method: String? = "간편결제"
    var totalAmount: Int = faker.random.nextInt(10_000, 50_000)
    var balanceAmount: Int = totalAmount
    var status: PaymentStatus = PaymentStatus.DONE
    var approvedAt: Instant = Instant.now()
    var card: CardInfo? = CardInfo(
        amount = totalAmount,
        company = "현대",
        number = "433012******1234",
        cardType = "신용",
        ownerType = "개인"
    )
    var easyPay: EasyPayInfo? = EasyPayInfo(
        amount = 0,
        provider = "네이버페이"
    )

    fun build() = Payment(
        version = version,
        paymentKey = paymentKey,
        orderId = orderId,
        method = method,
        totalAmount = totalAmount,
        balanceAmount = balanceAmount,
        status = status,
        approvedAt = approvedAt,
        card = card,
        easyPay = easyPay
    )
}
