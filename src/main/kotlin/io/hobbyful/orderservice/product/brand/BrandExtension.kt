package io.hobbyful.orderservice.product.brand

import io.hobbyful.orderservice.product.Product

fun Brand.toProductBrand() = Product.Brand(
    id = id,
    name = name
)