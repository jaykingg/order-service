package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.storeCredit.StoreCredit
import io.hobbyful.orderservice.storeCredit.transaction.TransactionView

inline fun storeCredit(block: StoreCreditFixture.() -> Unit = {}) = StoreCreditFixture().apply(block).build()

class StoreCreditFixture {
    var amount: Int = 0
    var transaction: TransactionView? = null

    fun build() = StoreCredit(
        amount = amount,
        transaction = transaction
    )
}