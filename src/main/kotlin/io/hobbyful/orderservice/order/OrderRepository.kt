package io.hobbyful.orderservice.order

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

interface OrderRepository : CoroutineCrudRepository<Order, ObjectId>, OrderPagingRepository, OrderManagementRepository {
    suspend fun findByNumber(orderNumber: String): Order?

    suspend fun findByIdAndCustomerId(orderId: ObjectId, customerId: String): Order?

    fun findAllByCustomerIdAndPaymentIsNotNull(customerId: String, pageable: Pageable): Flow<OrderListView>

    @Aggregation(
        """
            {
              ${'$'}search: {
                index: 'search-orders',
                compound: {
                  must: [{
                    text: {
                      query: ?0,
                      path: {
                        wildcard: '*'
                      }
                    }
                  }]
                }              
              }
            }
        """
    )
    fun searchAll(query: String, pageRequest: Pageable): Flow<Order>

    @Aggregation(
        pipeline = [
            "{ \$match: { 'placedAt': { \$gte: ?0, \$lte: ?1 }, 'status': { \$in: ?2 }}}",
            "{ \$unwind: '\$lineItems' }"
        ]
    )
    fun getAllToExport(
        placedAtFrom: Instant,
        placedAtUntil: Instant,
        status: Set<OrderStatus>,
        pageable: Pageable
    ): Flow<OrderExport>
}
