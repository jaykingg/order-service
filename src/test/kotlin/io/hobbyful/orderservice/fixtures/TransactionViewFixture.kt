package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.storeCredit.transaction.TransactionType
import io.hobbyful.orderservice.storeCredit.transaction.TransactionView
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

inline fun transactionView(block: TransactionViewFixture.() -> Unit = {}) = TransactionViewFixture().apply(block).build()

class TransactionViewFixture {
    val id: ObjectId = ObjectId.get()
    val orderId: ObjectId = ObjectId.get()
    val customerId: String = UUID.randomUUID().toString()
    val type: TransactionType = TransactionType.CHARGE
    val amount: Int = 10000
    val note: String = faker.random.toString()
    val createdAt: Instant = Instant.now()
    
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