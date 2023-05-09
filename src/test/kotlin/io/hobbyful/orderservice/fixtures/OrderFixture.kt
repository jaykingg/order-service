package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.order.Order
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.StoreType
import io.hobbyful.orderservice.order.refund.Refund
import io.hobbyful.orderservice.payment.Payment
import io.hobbyful.orderservice.shippingInfo.ShippingInfo
import io.hobbyful.orderservice.storeCredit.StoreCredit
import io.hobbyful.orderservice.summary.Summary
import org.bson.types.ObjectId
import java.time.Instant

/**
 * 주문 fixture
 */
inline fun order(block: OrderFixtureBuilder.() -> Unit = {}) = OrderFixtureBuilder().apply(block).build()

class OrderFixtureBuilder {
    var id: ObjectId? = null
    var number: String = Instant.now().toEpochMilli().toString()
    var customerId: String = faker.random.nextUUID()
    var cartId: ObjectId = ObjectId.get()
    var name: String = faker.commerce.productName()
    var storeCredit: StoreCredit = StoreCredit(0, null)
    var store: StoreType = StoreType.HOBBYFUL
    var status: OrderStatus = OrderStatus.PENDING
    var lineItems: List<LineItem> = listOf(lineItem())
    var shippingInfo: ShippingInfo? = shippingInfo()
    var summary: Summary = summary()
    var payment: Payment? = null
    var refund: Refund? = null
    var rewarded: Boolean = false
    var placedAt: Instant? = null
    var approvedAt: Instant? = null
    var closedAt: Instant? = null

    fun build() = Order(
        id = id,
        number = number,
        customerId = customerId,
        cartId = cartId,
        name = name,
        storeCredit = storeCredit,
        store = store,
        status = status,
        lineItems = lineItems,
        shippingInfo = shippingInfo,
        summary = summary,
        payment = payment,
        refund = refund,
        rewarded = rewarded,
        placedAt = placedAt ?: placedAtByStatus(status),
        approvedAt = approvedAt ?: approvedAtByStatus(status),
        closedAt = closedAt ?: closedAtByStatus(status),
    )

    fun paymentByStatus(status: OrderStatus): Payment? =
        when (status) {
            OrderStatus.PENDING, OrderStatus.CHECKOUT -> null
            else -> payment()
        }

    fun refundByStatus(status: OrderStatus): Refund? =
        if (status == OrderStatus.CANCELLED) refund() else null

    private fun placedAtByStatus(status: OrderStatus): Instant? =
        when (status) {
            OrderStatus.PENDING, OrderStatus.CHECKOUT -> null
            else -> Instant.now()
        }

    private fun approvedAtByStatus(status: OrderStatus): Instant? =
        when (status) {
            OrderStatus.PENDING, OrderStatus.CHECKOUT, OrderStatus.PAID -> null
            else -> Instant.now()
        }

    private fun closedAtByStatus(status: OrderStatus): Instant? =
        if (status == OrderStatus.CLOSED) Instant.now() else null
}
