package io.hobbyful.orderservice.order

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant
import javax.validation.constraints.Min
import javax.validation.constraints.Size

data class AdminExportCriteria(
    /**
     * 결제 완료 기간
     */
    @field: Size(min = 2, max = 2)
    val placedAtRange: List<Instant>,

    /**
     * 조회할 모든 주문 상태
     */
    val status: Set<OrderStatus> = emptySet(),

    @Schema(defaultValue = "0")
    @field: Min(0)
    val page: Int = 0
) {
    val placedAtFrom: Instant
        get() = placedAtRange[0]

    val placedAtUntil: Instant
        get() = placedAtRange[1]

    val pageRequest: PageRequest
        get() = PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, Order::number.name))

    fun replaceEmptyStatus(): Set<OrderStatus> = status.ifEmpty { OrderStatus.values().toSet() }
}