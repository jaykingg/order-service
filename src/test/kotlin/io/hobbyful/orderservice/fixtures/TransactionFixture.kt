package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.storeCredit.transaction.TransactionType
import io.hobbyful.orderservice.storeCredit.transaction.TransactionView
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

inline fun transaction(block: TransactionFixture.() -> Unit = {}) = TransactionFixture().apply(block).build()

class TransactionFixture {
    var id: ObjectId = ObjectId.get()
    var orderId: ObjectId = ObjectId.get()
    var customerId: String = UUID.randomUUID().toString()
    var type: TransactionType = TransactionType.CHARGE
    var amount: Int = 1000
    var note: String = faker.random.randomString()
    var createdAt: Instant = Instant.now()

    fun build() = TransactionView(
        id = id,
        orderId = orderId,
        customerId = customerId,
        type = type,
        amount = amount,
        note = note,
        createdAt = createdAt
    )
}