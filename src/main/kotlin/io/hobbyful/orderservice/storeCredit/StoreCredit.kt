package io.hobbyful.orderservice.storeCredit

import io.hobbyful.orderservice.storeCredit.transaction.TransactionView
import javax.validation.Valid
import javax.validation.constraints.PositiveOrZero

/**
 * 주문의 적립금 사용 정보
 *
 * @property amount
 * @property transaction
 * @constructor Create empty Store credit
 */
data class StoreCredit(
    /**
     * 사용할 적립금액
     */
    @field: PositiveOrZero
    val amount: Int,

    /**
     * 적립금 차감 내역
     */
    @field: Valid
    val transaction: TransactionView?
) {
    companion object {
        fun of(
            amount: Int = 0,
            transaction: TransactionView? = null
        ) = StoreCredit(
            amount = amount,
            transaction = transaction
        )
    }
}
