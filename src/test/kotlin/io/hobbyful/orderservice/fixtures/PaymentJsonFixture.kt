package io.hobbyful.orderservice.fixtures

import org.bson.types.ObjectId

/**
 * 토스페이먼츠 결제 객체의 샘플 JSON body fixture
 */
inline fun paymentJson(block: PaymentJsonFixtureBuilder.() -> Unit = {}) =
    PaymentJsonFixtureBuilder().apply(block).build()

class PaymentJsonFixtureBuilder {
    var paymentKey = faker.random.randomString()
    var orderId = ObjectId.get()
    var totalAmount = faker.random.nextInt(10_000, 100_000)

    fun build() = """
        {
          "mId": "tosspayments",
          "version": "2022-06-08",
          "transactionKey": "B7103F204998813B889C77C043D09502",
          "lastTransactionKey": "B7103F204998813B889C77C043D09502",
          "paymentKey": "$paymentKey",
          "orderId": "$orderId",
          "orderName": "토스 티셔츠 외 2건",
          "currency": "KRW",
          "method": "카드",
          "status": "DONE",
          "requestedAt": "2021-01-01T10:01:30+09:00",
          "approvedAt": "2021-01-01T10:05:40+09:00",
          "useEscrow": false,
          "cultureExpense": false,
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
          "transfer": null,
          "mobilePhone": null,
          "giftCertificate": null,
          "foreignEasyPay": null,
          "cashReceipt": null,
          "receipt": {
            "url": "https://merchants.tosspayments.com/web/serve/merchant/test_ck_OAQ92ymxN340ewLDLN43ajRKXvdk/receipt/5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6"
          },
          "discount": null,
          "cancels": [
              {
                "cancelReason": "고객이 취소를 원함",
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
          "type": "NORMAL",
          "easyPay": null,
          "country": "KR",
          "failure": null,
          "totalAmount": $totalAmount,
          "balanceAmount": 15000,
          "suppliedAmount": 13636,
          "vat": 1364,
          "taxFreeAmount": 0
        }
    """.trimIndent()
}
