package io.hobbyful.orderservice.order

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class OrderQueryService(
    private val orderRepository: OrderRepository
) {
    suspend fun getAllFiltered(criteria: AdminFilterCriteria): Page<Order> =
        orderRepository.getAllFiltered(criteria)

    fun searchAll(criteria: AdminSearchCriteria): Flow<Order> =
        orderRepository.searchAll(criteria.query, criteria.pageRequest)

    fun getAllToExport(criteria: AdminExportCriteria): Flow<OrderExport> {
        return orderRepository.getAllToExport(
            criteria.placedAtFrom,
            criteria.placedAtUntil,
            criteria.replaceEmptyStatus(),
            criteria.pageRequest
        )
    }
}
