package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.payment.PaymentStatus
import io.hobbyful.orderservice.payment.TossWebhookEventType
import java.time.Instant

inline fun tossWebhookJson(block: TossWebhookJsonFixture.() -> Unit = {}) =
    TossWebhookJsonFixture().apply(block).build()

class TossWebhookJsonFixture {
    var eventType: TossWebhookEventType = TossWebhookEventType.PAYMENT_STATUS_CHANGED
    var createdAt: String = "2022-01-01T00:00:00.000000"
    var paymentKey = faker.random.randomString()
    var orderId: String = "HF${Instant.now().toEpochMilli()}"
    var status: PaymentStatus = PaymentStatus.CANCELED
    var cancelledReason: String? = "고객이 취소를 원함"

    fun build() = """
        {
            "eventType": "$eventType",
            "createdAt": "$createdAt",
            "data": {
                "mId": "tosspayments",
                "version": "2022-11-16",
                "lastTransactionKey": "B7103F204998813B889C77C043D09502",
                "paymentKey": "$paymentKey",
                "orderId": "$orderId",
                "status": "$status",
                "requestedAt": "2022-08-05T12:56:00+09:00",
                "approvedAt": "2022-08-05T12:56:21+09:00",
                "useEscrow": false,
                "card": {
                    "amount": 15000,
                    "company": "현대",
                    "number": "433012******1234",
                    "installmentPlanMonths": 0,
                    "isInterestFree": false,
                    "interestPayer": null,
                    "approveNo": "00000000",
                    "useCardPoint": false,
                    "cardType": "신용",
                    "ownerType": "개인",
                    "acquireStatus": "READY",
                    "receiptUrl": "https://merchants.tosspayments.com/web/serve/merchant/test_ck_OAQ92ymxN340ewLDLN43ajRKXvdk/receipt/5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6"
                },
                "virtualAccount": null,
                "mobilePhone": null,
                "giftCertificate": null,
                "cashReceipt": null,
                "discount": null,
                "cancels": [
                    {
                        "cancelReason": "$cancelledReason",
                        "canceledAt": "2022-01-01T11:32:04+09:00",
                        "cancelAmount": 10000,
                        "taxFreeAmount": 0,
                        "taxAmount": null,
                        "refundableAmount": 0,
                        "easyPayDiscountAmount": 0,
                        "transactionKey": "8B4F646A829571D870A3011A4E13D640"
                    }
                ],
                "secret": null,
                "useDiscount": false,
                "discountAmount": 0,
                "useCashReceipt": false,
                "isPartialCancelable": true,
                "receipt": {
                  "url": "https://dashboard.tosspayments.com/sales-slip?transactionId=iGPzjWDJfI%2BI1Aae1cHkfdAC5oig85qknxR5o0ArYVlw%2FnRsRh8NkNIjsW0YsUJY&ref=PX"
                },
                "checkout": {
                  "url": "http://api.tosspayments.com/v1/payments/_Wfyb_yPVgIz1feTVH63h/checkout"
                },
                "currency": "KRW",
                "totalAmount": 10000,
                "balanceAmount": 10000,
                "method": "카드"
            }
        }
    """.trimIndent()
}