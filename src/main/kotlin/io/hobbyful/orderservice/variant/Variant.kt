package io.hobbyful.orderservice.variant

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.MongoId
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

/**
 * 상품 정보
 */
data class Variant(
    /**
     * 상품 ID
     */
    @MongoId
    val id: ObjectId,

    /**
     * 브랜드 ID
     */
    val brandId: ObjectId,

    /**
     * 준비물 ID
     */
    val productId: ObjectId,

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
