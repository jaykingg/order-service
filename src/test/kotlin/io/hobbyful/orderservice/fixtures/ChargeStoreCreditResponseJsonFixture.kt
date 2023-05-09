package io.hobbyful.orderservice.fixtures

import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

inline fun chargeStoreCreditResponseJson(block: ChargeStoreCreditResponseJsonFixture.() -> Unit = {}) = ChargeStoreCreditResponseJsonFixture().apply(block).build()

class ChargeStoreCreditResponseJsonFixture {
    var transactionId: ObjectId = ObjectId.get()
    var orderId: ObjectId = ObjectId.get()
    var customerId: String = UUID.randomUUID().toString()
    var amount: Int = 1000
    var note: String = faker.random.toString()
    var createdAt: Instant = Instant.now()

    fun build() = """
        {
          "id" : "$transactionId",
          "customerId" : "$customerId",
          "orderId" : "$orderId",
          "type" : "charge",
          "amount" : "$amount",
          "note" : "$note",
          "createdAt" : "$createdAt"
        }
    """.trimIndent()
}