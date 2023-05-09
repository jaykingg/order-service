package io.hobbyful.orderservice.product.brand

import org.bson.types.ObjectId

/**
 * 브랜드 정보
 */
data class Brand(
    /**
     * 브랜드 ID
     */
    val id: ObjectId,

    /**
     * 브랜드 이름
     */
    val name: String
)
