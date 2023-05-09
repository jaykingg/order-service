package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.variant.VariantView
import org.bson.types.ObjectId
import java.util.*

inline fun variantView(block: VariantViewFixture.() -> Unit = {}) =
    VariantViewFixture().apply(block).build()

class VariantViewFixture {
    var id: ObjectId = ObjectId.get()
    var name: String = faker.commerce.productName()
    var basePrice: Int = 5000
    var price: Int = 10000
    var primary: Boolean = true
    var sku: String = UUID.randomUUID().toString()

    fun build() = VariantView(
        id = id,
        name = name,
        basePrice = basePrice,
        price = price,
        primary = primary,
        sku = sku
    )
}
