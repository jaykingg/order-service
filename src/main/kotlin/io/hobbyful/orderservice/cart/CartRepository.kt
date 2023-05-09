package io.hobbyful.orderservice.cart

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CartRepository : CoroutineCrudRepository<Cart, ObjectId> {
    /**
     * 구매 선택된 lineItems만 포함된 고객의 장바구니 조회
     *
     * @param customerId
     * @return
     */
    @Aggregation(
        pipeline = [
            "{ \$match: { 'customerId': ?0 } }",
            "{ \$addFields: { 'lineItems': { \$filter: { input: '\$lineItems', as: 'item', cond: { \$eq: [ '\$\$item.picked', true ] } } } } }"
        ]
    )
    suspend fun findByCustomerIdToCheckout(customerId: String): Cart?
}
