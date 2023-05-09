package io.hobbyful.orderservice.product

import io.hobbyful.orderservice.core.NotFoundException
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    suspend fun getViewById(productId: ObjectId): ProductView =
        productRepository.getViewById(productId) ?: throw NotFoundException()
}
