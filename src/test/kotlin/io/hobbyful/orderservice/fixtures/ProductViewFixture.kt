package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.product.Product
import io.hobbyful.orderservice.product.ProductView
import io.hobbyful.orderservice.product.brand.toProductBrand
import org.bson.types.ObjectId

inline fun productView(block: ProductViewFixture.() -> Unit = {}) =
    ProductViewFixture().apply(block).build()

class ProductViewFixture {
    var id: ObjectId = ObjectId.get()
    var brand: Product.Brand = brand().toProductBrand()
    var brandAdjustmentRate: Double = 0.0
    var name: String = faker.commerce.productName()
    var featuredImage: String = "https://test-featureimg.com"
    var videoPlaylist: String? = "https://test-viedoplaylist.com"

    fun build() = ProductView(
        id = id,
        brand = brand,
        brandAdjustmentRate = brandAdjustmentRate,
        name = name,
        featuredImage = featuredImage,
        videoPlaylist = videoPlaylist
    )
}