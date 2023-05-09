package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.product.ProductView
import io.hobbyful.orderservice.variant.VariantView

/**
 * 주문품목 fixture
 */
inline fun lineItem(block: LineItemFixtureBuilder.() -> Unit = {}) =
    LineItemFixtureBuilder().apply(block).build()

class LineItemFixtureBuilder {
    var productView: ProductView = productView()
    var variantView: VariantView = variantView()
    var quantity: Int = 1
    var picked: Boolean = true

    fun build() = LineItem(
        product = productView,
        variant = variantView,
        quantity = quantity,
        picked = picked
    )
}
