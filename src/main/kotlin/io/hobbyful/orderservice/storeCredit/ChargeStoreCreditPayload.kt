package io.hobbyful.orderservice.storeCredit

import org.bson.types.ObjectId
import javax.validation.constraints.Positive

data class ChargeStoreCreditPayload(
    /**
     * 주문 ID
     */
    val orderId: ObjectId,

    /**
     * 사용할 적립금액
     */
    @field: Positive
    val amount: Int
)
