package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.cart.Cart
import io.hobbyful.orderservice.lineitem.LineItem
import org.bson.types.ObjectId

/**
 * 장바구니 fixture
 */
inline fun cart(block: CartFixtureBuilder.() -> Unit = {}) = CartFixtureBuilder().apply(block).build()

class CartFixtureBuilder {
    var id: ObjectId = ObjectId.get()
    var customerId: String = faker.random.nextUUID()
    var lineItems: List<LineItem> = listOf(lineItem())

    fun build() = Cart(
        id = id,
        customerId = customerId,
        lineItems = lineItems
    )
}
