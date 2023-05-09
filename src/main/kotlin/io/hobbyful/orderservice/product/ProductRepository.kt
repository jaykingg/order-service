package io.hobbyful.orderservice.product

import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface ProductRepository : CoroutineSortingRepository<Product, ObjectId> {

    suspend fun getViewById(id: ObjectId): ProductView?
}
