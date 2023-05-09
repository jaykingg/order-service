package io.hobbyful.orderservice.product.brand

import org.bson.types.ObjectId

/**
 * 브랜드 정보 응답 데이터
 */
data class BrandView(
    /**
     * 브랜드 ID
     */
    val id: ObjectId,

    /**
     * 브랜드 이름
     */
    val name: String
)

fun Brand.toBrandView(): BrandView = BrandView(
    id = id,
    name = name
)
