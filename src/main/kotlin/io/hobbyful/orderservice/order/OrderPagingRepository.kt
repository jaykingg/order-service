package io.hobbyful.orderservice.order

import org.springframework.data.domain.Page

interface OrderPagingRepository {
    suspend fun getAllFiltered(parameter: AdminFilterCriteria): Page<Order>
}
