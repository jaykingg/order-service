package io.hobbyful.orderservice.order

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.flow
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.*
import org.springframework.stereotype.Repository

@Repository
class OrderPagingRepositoryImpl(
    @Qualifier("reactiveMongoTemplate")
    private val ops: ReactiveMongoTemplate
) : OrderPagingRepository {
    override suspend fun getAllFiltered(parameter: AdminFilterCriteria): Page<Order> {
        var criteria = where(Order::placedAt)
            .gte(parameter.placedAtFrom)
            .lte(parameter.placedAtUntil)

        if (parameter.status.isNotEmpty()) {
            criteria = criteria.and(Order::status).inValues(parameter.status)
        }

        return paginatedQuery(Query(criteria), parameter.pageRequest)
    }

    private suspend fun paginatedQuery(query: Query, pageable: Pageable): Page<Order> =
        coroutineScope {
            /**
             * WARN: Query 객체에 clone 기능이 없어 pageable이 적용된 것과 적용되지 않은 것으로 각각 선언해야 합니다.
             */
            val content = async { ops.query<Order>().matching(Query.of(query).with(pageable)).flow().toList() }
            val totalCount = async { ops.query<Order>().matching(query).count().awaitSingle() }

            PageImpl(content.await(), pageable, totalCount.await())
        }
}
