package io.hobbyful.orderservice.order

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Range
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import javax.validation.constraints.Min

data class OrderCriteria(
    /**
     * 페이지 인덱스
     */
    @Schema(defaultValue = "0")
    @field: Min(0)
    val page: Int = 0,

    /**
     * 페이지당 조회할 준비물 개수
     */
    @field: Schema(allowableValues = ["5", "10", "15", "20", "25"], defaultValue = "25")
    @field: Range(min = 5, max = 25)
    val size: Int = 25
) {
    fun toPageRequest(sort: Sort = Sort.unsorted()) = PageRequest.of(page, size, sort)
}
