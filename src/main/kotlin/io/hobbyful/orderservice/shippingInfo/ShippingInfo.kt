package io.hobbyful.orderservice.shippingInfo

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

/**
 * 배송정보
 */
data class ShippingInfo(
    /**
     * 수령인
     */
    @field: NotBlank
    val recipient: String,

    /**
     * 휴대폰 번호
     */
    @field: NotBlank
    @field: Pattern(regexp = """^0\d{9,10}$""", message = "유효하지 않은 휴대전화 번호입니다")
    val primaryPhoneNumber: String,

    /**
     * 연락처
     */
    @field: Pattern(regexp = """^0\d{8,10}$""", message = "유효하지 않은 전화번호입니다")
    val secondaryPhoneNumber: String? = null,

    /**
     * 우편 번호
     */
    @field: NotBlank
    @field: Pattern(regexp = """^\d{5}$""", message = "유효하지 않은 우편번호입니다")
    val zipCode: String,

    /**
     * 주소
     */
    @field: NotBlank
    val line1: String,

    /**
     * 나머지 주소
     */
    val line2: String? = null,

    /**
     * 배송시 요구사항
     */
    val note: String? = null
)
