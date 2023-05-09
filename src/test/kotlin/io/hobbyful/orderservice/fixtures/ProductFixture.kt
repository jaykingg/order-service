package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.product.Product
import io.hobbyful.orderservice.product.brand.toProductBrand
import org.bson.types.ObjectId

/**
 * 준비물 fixture
 */
inline fun product(block: ProductFixtureBuilder.() -> Unit = {}) =
    ProductFixtureBuilder().apply(block).build()

class ProductFixtureBuilder {
    var id: ObjectId = ObjectId.get()
    var brand: Product.Brand = brand().toProductBrand()
    var brandAdjustmentRate: Double = 0.0
    var name = faker.commerce.productName()
    var featuredImage = "https://picsum.photos/300"
    var videoPlaylist = "https://videoplaylist/123456"

    fun build() = Product(
        id = id,
        brand = brand,
        brandAdjustmentRate = brandAdjustmentRate,
        name = name,
        featuredImage = featuredImage,
        videoPlaylist = videoPlaylist
    )
}
