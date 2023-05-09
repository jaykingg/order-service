package io.hobbyful.orderservice.lineitem

import org.bson.types.ObjectId

/**
 * 주문 품목의 준비물 Id
 */
val LineItem.productId: ObjectId
    get() = product.id

/**
 * 주문 품목의 준비물 이름
 */
val LineItem.productName: String
    get() = product.name

/**
 * 주문 품목이 준비물의 필수 상품인지 여부
 */
val LineItem.isPrimary: Boolean
    get() = variant.primary

/**
 * 주문 품목 리스트 총액
 */
val Collection<LineItem>.subtotal: Int
    get() = sumOf { it.total }

/**
 * 첫번째 주문품목의 준비물 이름
 */
val Collection<LineItem>.firstProductName: String
    get() = firstOrNull()?.productName ?: ""
