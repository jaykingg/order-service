package io.hobbyful.orderservice.config

import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = [
        "io.hobbyful.orderservice.product",
        "io.hobbyful.orderservice.variant"
    ],
    reactiveMongoTemplateRef = "catalogMongoTemplate"
)
class CatalogMongoTemplateConfig(
    private val mongoClient: MongoClient
) {
    private val database = "catalog-db"

    @Bean
    fun catalogMongoTemplate(): ReactiveMongoTemplate = ReactiveMongoTemplate(
        SimpleReactiveMongoDatabaseFactory(mongoClient, database)
    )
}
