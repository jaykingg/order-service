package io.hobbyful.orderservice.lineitem

import io.hobbyful.orderservice.product.ProductView
import io.hobbyful.orderservice.variant.VariantView
import org.hibernate.validator.constraints.Range
import javax.validation.Valid
import javax.validation.constraints.AssertTrue

/**
 * 주문 품목 관리
 */
data class LineItem(
    /**
     * 준비물 정보
     */
    @field: Valid
    val product: ProductView,

    /**
     * 상품 정보
     */
    @field: Valid
    val variant: VariantView,

    /**
     * 구매수량
     */
    @field: Range(min = 1, max = 999)
    val quantity: Int,

    /**
     * 구매 선택 여부
     */
    @field: AssertTrue
    val picked: Boolean,
) {
    companion object {
        fun of(
            product: ProductView,
            variant: VariantView,
            quantity: Int,
            picked: Boolean
        ): LineItem = LineItem(
            product = product,
            variant = variant,
            quantity = quantity,
            picked = picked
        )
    }

    /**
     * 주문 품목 총액
     */
    val total: Int
        get() = variant.price * quantity
}
