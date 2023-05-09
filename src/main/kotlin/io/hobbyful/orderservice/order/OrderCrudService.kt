package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.core.NotFoundException
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

/**
 * 주문 CRUD 서비스
 */
@Service
class OrderCrudService(
    private val orderRepository: OrderRepository
) {
    suspend fun getByNumber(orderNumber: String): Order =
        orderRepository.findByNumber(orderNumber) ?: throw NotFoundException()

    suspend fun getByIdAndCustomerId(orderId: ObjectId, customerId: String): Order =
        orderRepository.findByIdAndCustomerId(orderId, customerId) ?: throw NotFoundException()

    fun getAllByCustomerIdAndPaid(customerId: String, criteria: OrderCriteria): Flow<OrderListView> =
        orderRepository.findAllByCustomerIdAndPaymentIsNotNull(
            customerId,
            criteria.toPageRequest(sort = Sort.by("number").descending())
        )

    suspend fun save(order: Order): Order = orderRepository.save(order)
}
