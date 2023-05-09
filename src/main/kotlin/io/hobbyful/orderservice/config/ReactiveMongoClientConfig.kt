package io.hobbyful.orderservice.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.ConstraintViolationException

/**
 * Reactive Mongo Configuration
 *
 * - 공통으로 사용할 `reactiveMongoClient` Bean 제공
 * - 서비스의 메인 DB에 사용할 `reactiveMongoTemplate` Bean 제공
 */
@Configuration
@EnableReactiveMongoAuditing
class ReactiveMongoClientConfig(
    private val properties: MongoProperties
) : AbstractReactiveMongoConfiguration() {
    override fun getDatabaseName(): String = properties.database

    override fun autoIndexCreation(): Boolean = properties.isAutoIndexCreation

    override fun configureClientSettings(builder: MongoClientSettings.Builder) {
        builder.applyConnectionString(ConnectionString(properties.uri))
    }

    /**
     * Reactive Mongo Transaction Manager 활성화
     *
     * Connection string에 `replicaSet` 설정이 존재하고, 활성화 되어 있어야 @Transactional 사용 가능
     *
     * [Reactive Transactions](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.transactions.reactive)
     *
     * [Transactions with ReactiveMongoTransactionManager](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.transactions.reactive-tx-manager)
     */
    @Bean
    fun reactiveMongoTransactionManager(
        reactiveMongoDatabaseFactory: ReactiveMongoDatabaseFactory
    ): ReactiveMongoTransactionManager =
        ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory)

    /**
     * ### `javax.validation 기반` Entity Validator
     * `entity`가 `database`에 저장되기 전에 validation 체크를 수행하며,
     * validation 실패시 [ConstraintViolationException] 발생
     */
    @Bean
    fun validatingMongoEventListener(
        localValidatorFactoryBean: LocalValidatorFactoryBean
    ): ValidatingMongoEventListener = ValidatingMongoEventListener(localValidatorFactoryBean)
}
