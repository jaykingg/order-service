package io.hobbyful.orderservice.product

import com.fasterxml.jackson.annotation.JsonView
import io.hobbyful.orderservice.core.Views
import org.bson.types.ObjectId
import org.hibernate.validator.constraints.URL
import javax.validation.constraints.NotBlank

data class ProductView(
    /**
     * 준비물 ID
     */
    val id: ObjectId,

    /**
     * 브랜드 정보
     */
    val brand: Product.Brand,

    /**
     * 브랜드 정산율
     */
    @JsonView(Views.Internal::class)
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
)
