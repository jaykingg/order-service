package io.hobbyful.orderservice.cart

import io.hobbyful.orderservice.lineitem.LineItem
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.MongoId
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

/**
 * 장바구니
 */
data class Cart(
    /**
     * 장바구니 ID
     */
    @MongoId
    val id: ObjectId,

    /**
     * 고객 ID
     */
    @field: NotBlank
    val customerId: String,

    /**
     * 주문 상품 리스트
     */
    @field: NotEmpty
    @field: PrimaryVariantConstraint
    @field: Valid
    val lineItems: List<LineItem>
)
