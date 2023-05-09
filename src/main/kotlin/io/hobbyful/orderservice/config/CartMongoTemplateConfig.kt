package io.hobbyful.orderservice.config

import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = ["io.hobbyful.orderservice.cart"],
    reactiveMongoTemplateRef = "cartMongoTemplate"
)
class CartMongoTemplateConfig(
    private val reactiveMongoClient: MongoClient
) {
    private val database = "cart-db"

    @Bean
    fun cartMongoTemplate(): ReactiveMongoTemplate = ReactiveMongoTemplate(
        SimpleReactiveMongoDatabaseFactory(reactiveMongoClient, database)
    )
}
