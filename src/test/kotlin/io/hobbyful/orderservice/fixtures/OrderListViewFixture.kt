package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.order.OrderListView
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.payment.PaymentView
import org.bson.types.ObjectId
import java.time.Instant

inline fun orderListView(block: OrderListViewFixture.() -> Unit = {}) = OrderListViewFixture().apply(block).build()

class OrderListViewFixture {
    var id: ObjectId = ObjectId.get()
    var number: String = Instant.now().toEpochMilli().toString()
    var name: String = faker.commerce.productName()
    var status: OrderStatus = OrderStatus.PAID
    var payment: PaymentView? = paymentView()

    fun build() = OrderListView(
        id = id,
        number = number,
        name = name,
        status = status,
        payment = payment
    )
}