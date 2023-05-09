package io.hobbyful.orderservice.variant

import org.bson.types.ObjectId
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

/**
 * 상품정보의 응답데이터
 */
data class VariantView(
    /**
     * 상품 ID
     */
    val id: ObjectId,

    /**
     * 상품 이름
     */
    @field: NotBlank
    val name: String,

    /**
     * 기본 가격
     */
    @field: Positive
    val basePrice: Int,

    /**
     * 판매 가격
     */
    @field: Positive
    val price: Int,

    /**
     * 준비물의 필수 상품 여부
     */
    val primary: Boolean,

    /**
     * 재고관리 번호
     */
    val sku: String
)
