package io.hobbyful.orderservice.product

import org.bson.types.ObjectId
import org.hibernate.validator.constraints.URL
import org.springframework.data.mongodb.core.index.Indexed
import javax.validation.constraints.NotBlank

data class Product(
    /**
     * 준비물 ID
     */
    val id: ObjectId,

    /**
     * 브랜드 정보
     */
    val brand: Brand,

    /**
     * 브랜드 정산율
     */
    val brandAdjustmentRate: Double,

    /**
     * 준비물 이름
     */
    @field: NotBlank
    val name: String,

    /**
     * 메인 이미지
     */
    @field: NotBlank
    val featuredImage: String,

    /**
     * 재생목록 URL
     */
    @field: URL
    val videoPlaylist: String?
) {
    data class Brand(
        @Indexed
        val id: ObjectId,

        @Indexed
        val name: String,
    )
}
