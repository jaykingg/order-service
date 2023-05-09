package io.hobbyful.orderservice.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = ["io.hobbyful.orderservice.order"]
)
class OrderMongoTemplateConfig
