package io.hobbyful.orderservice.variant

import io.hobbyful.orderservice.core.NotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class VariantService(
    private val variantRepository: VariantRepository
) {
    /**
     * 준비물 ID + 주문상품 ID 리스트로 모든 상품 조회
     *
     * @param productId 준비물 ID
     * @param variantIds 주문상품 ID 리스트
     * @exception [NotFoundException] productId + variantId 조합으로 존재하지 않는 주문상품이 있는 경우
     */
    suspend fun getAllViewByIdsAndProductId(variantIds: Collection<ObjectId>, productId: ObjectId): Flow<VariantView> =
        variantRepository.getAllViewByIdInAndProductId(variantIds, productId)
            .also { if (it.count() != variantIds.size) throw NotFoundException() }
}
