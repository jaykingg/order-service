package io.hobbyful.orderservice.payment

data class TransferInfo(
    /**
     * 이체할 은행
     */
    val bank: String,

    /**
     * 정산 상태
     */
    val settlementStatus: String
)
