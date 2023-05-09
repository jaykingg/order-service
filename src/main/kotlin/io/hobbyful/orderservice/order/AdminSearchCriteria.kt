package io.hobbyful.orderservice.order

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class AdminSearchCriteria(
    @field: NotBlank
    val query: String,

    @field: Min(0)
    val page: Int = 0
) {
    val pageRequest: PageRequest
        get() = PageRequest.of(page, 15, Sort.unsorted())
}