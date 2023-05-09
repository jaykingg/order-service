package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.payment.Payment
import io.hobbyful.orderservice.payment.PaymentStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mapping.div
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.allAndAwait
import org.springframework.data.mongodb.core.query.*
import org.springframework.data.mongodb.core.update
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class OrderManagementRepositoryImpl(
    @Qualifier("reactiveMongoTemplate")
    private val ops: ReactiveMongoTemplate
) : OrderManagementRepository {
    override suspend fun approveAllPaid(): Long =
        ops.update<Order>()
            .matching(
                where(Order::status).isEqualTo(OrderStatus.PAID)
                    .and(Order::payment / Payment::status).isEqualTo(PaymentStatus.DONE)
            )
            .apply(
                Instant.now().let { currentInstant ->
                    Update()
                        .set(Order::status.name, OrderStatus.PLANNING)
                        .set(Order::approvedAt.name, currentInstant)
                        .set(Order::updatedAt.name, currentInstant)
                }
            )
            .allAndAwait()
            .modifiedCount

    override suspend fun closeAllDelivered(): Long =
        ops.update<Order>()
            .matching(where(Order::status).isEqualTo(OrderStatus.DELIVERED))
            .apply(
                Instant.now().let { currentInstant ->
                    Update()
                        .set(Order::status.name, OrderStatus.CLOSED)
                        .set(Order::closedAt.name, currentInstant)
                        .set(Order::updatedAt.name, currentInstant)
                }
            )
            .allAndAwait()
            .modifiedCount

    override suspend fun setShippingByNumbers(numbers: Collection<String>): Long =
        ops.update<Order>()
            .matching(
                where(Order::number).inValues(numbers)
                    .and(Order::status).isEqualTo(OrderStatus.PLANNING)
            )
            .apply(
                Update()
                    .set(Order::status.name, OrderStatus.SHIPPING)
                    .set(Order::updatedAt.name, Instant.now())
            )
            .allAndAwait()
            .modifiedCount

    override suspend fun setDeliveredByNumbers(numbers: Collection<String>): Long =
        ops.update<Order>()
            .matching(
                where(Order::number).inValues(numbers)
                    .and(Order::status).isEqualTo(OrderStatus.SHIPPING)
            )
            .apply(
                Update()
                    .set(Order::status.name, OrderStatus.DELIVERED)
                    .set(Order::updatedAt.name, Instant.now())
            )
            .allAndAwait()
            .modifiedCount
}
