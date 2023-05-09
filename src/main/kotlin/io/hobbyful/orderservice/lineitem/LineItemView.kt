package io.hobbyful.orderservice.lineitem

import io.hobbyful.orderservice.product.ProductView
import io.hobbyful.orderservice.variant.VariantView

/**
 * 주문 품목 Response Body
 */
data class LineItemView(
    /**
     * 준비물 ID
     */
    val product: ProductView,

    /**
     * 상품 정보
     */
    val variant: VariantView,

    /**
     * 구매수량
     */
    val quantity: Int,

    /**
     * 주문 품목 총액
     */
    val total: Int
)
