package io.hobbyful.orderservice.variant

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface VariantRepository : CoroutineSortingRepository<Variant, ObjectId> {
    fun getAllViewByIdInAndProductId(variantIds: Collection<ObjectId>, productId: ObjectId): Flow<VariantView>
}
