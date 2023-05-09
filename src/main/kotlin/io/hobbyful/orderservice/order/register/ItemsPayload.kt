package io.hobbyful.orderservice.order.register

import org.bson.types.ObjectId
import org.hibernate.validator.constraints.Range
import org.hibernate.validator.constraints.UniqueElements
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class ItemsPayload(
    /**
     * 주문상품의 준비물 ID
     */
    val productId: ObjectId,

    /**
     * 주문상품 리스트
     */
    @field: Valid
    @field: NotEmpty
    @field: UniqueElements
    val items: List<Item>
) {
    /**
     * 주문상품 정보
     */
    data class Item(
        /**
         * 상품 ID
         */
        val variantId: ObjectId,

        /**
         * 상품 수량
         */
        @field: Range(min = 1, max = 999)
        val quantity: Int
    ) {
        /**
         * UniqueElements 유효성 검사에서, variantId의 중복 여부만 확인하기 위해 추가
         */
        override fun equals(other: Any?): Boolean =
            other is Item && other.variantId == variantId

        override fun hashCode(): Int = variantId.hashCode()
    }
}

val ItemsPayload.variantIds: List<ObjectId>
    get() = items.map { it.variantId }
