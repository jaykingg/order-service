package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.variant.Variant
import org.bson.types.ObjectId
import java.util.*

/**
 * 상품 fixture
 */
inline fun variant(block: VariantFixtureBuilder.() -> Unit = {}) =
    VariantFixtureBuilder().apply(block).build()

class VariantFixtureBuilder {
    var id: ObjectId = ObjectId.get()
    var brandId: ObjectId = ObjectId.get()
    var productId: ObjectId = ObjectId.get()
    var name: String = faker.commerce.productName()
    var basePrice: Int = 10_000
    var price: Int = 9_000
    var primary: Boolean = true
    var sku: String = UUID.randomUUID().toString()

    fun build() = Variant(
        id = id,
        brandId = brandId,
        productId = productId,
        name = name,
        basePrice = basePrice,
        price = price,
        primary = primary,
        sku = sku
    )
}
