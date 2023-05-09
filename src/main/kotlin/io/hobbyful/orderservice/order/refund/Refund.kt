package io.hobbyful.orderservice.order.refund

import org.hibernate.validator.constraints.Length
import org.springframework.data.annotation.CreatedDate
import java.time.Instant
import javax.validation.constraints.NotBlank

/**
 * 주문 취소
 */
data class Refund(
    /**
     * 취소 유형
     */
    @field: NotBlank
    val reason: String,

    /**
     * 상세 내용
     */
    @field: Length(min = 1, max = 500)
    val message: String? = null,

    /**
     * 취소 승인 시간
     */
    @CreatedDate
    val approvedAt: Instant? = null
) {
    companion object {
        fun of(reason: String) = Refund(
            reason = reason
        )
    }
}
