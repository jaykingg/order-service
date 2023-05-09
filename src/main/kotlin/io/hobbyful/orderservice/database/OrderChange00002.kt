package io.hobbyful.orderservice.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import io.mongock.driver.mongodb.reactive.util.MongoSubscriberSync
import org.bson.Document
import org.springframework.core.io.ClassPathResource

/**
 * 결제가 정상적으로 완료된 모든 주문에 `placedAt` 필드를 추가합니다.
 */
@ChangeUnit(id = "order-change-00002", order = "00002")
class OrderChange00002 {
    private val resourcePath = "migration/order-change-00002.json"

    @Execution
    fun execution(mapper: ObjectMapper, mongoDatabase: MongoDatabase) {
        val query = mapper.readValue<JsonQuery>(ClassPathResource(resourcePath).file)
        val subscriber = MongoSubscriberSync<UpdateResult>()

        mongoDatabase.getCollection("order")
            .updateMany(query.filter, query.update)
            .subscribe(subscriber)

        subscriber.await()
    }

    @RollbackExecution
    fun rollback() = Unit

    data class JsonQuery(
        val filter: Document,
        val update: List<Document>
    )
}
