package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.order.register.ItemsPayload
import org.bson.types.ObjectId

class ItemPayloadFixture {
    var productId: ObjectId = ObjectId.get()

    fun build() = ItemsPayload(
        productId = productId,
        items = listOf(
            ItemsPayload.Item(
                variantId = ObjectId.get(),
                quantity = 5
            )
        )
    )
}

inline fun itemPayloadFixture(block: ItemPayloadFixture.() -> Unit = {}) =
    ItemPayloadFixture().apply(block).build()
