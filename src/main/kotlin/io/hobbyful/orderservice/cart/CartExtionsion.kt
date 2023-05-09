package io.hobbyful.orderservice.cart

import io.hobbyful.orderservice.lineitem.LineItem

/**
 * 구매 선택된 품목 리스트
 */
val Cart.pickedLineItems: List<LineItem>
    get() = lineItems.filter { it.picked }
