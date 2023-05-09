package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.product.brand.Brand
import org.bson.types.ObjectId

/**
 * 브랜드 fixture
 */
inline fun brand(block: BrandFixtureBuilder.() -> Unit = {}) = BrandFixtureBuilder().apply(block).build()

class BrandFixtureBuilder {
    var id: ObjectId = ObjectId.get()
    var name: String = faker.commerce.brand()

    fun build() = Brand(
        id = id,
        name = name
    )
}
