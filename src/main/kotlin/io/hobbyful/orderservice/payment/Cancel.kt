package io.hobbyful.orderservice.payment

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.Instant

/**
 * 결제 취소 정보
 */
data class Cancel(
    /**
     * 취소 거래 구분을 위한 Id
     */
    val transactionKey: String,

    /**
     * 결제를 취소한 이유
     */
    val cancelReason: String,

    /**
     * 결제를 취소한 금액
     */
    val cancelAmount: Int,

    /**
     * 결제 취소 시간
     */
    @field: JsonDeserialize(using = IsoDateDeserializer::class)
    val canceledAt: Instant
)
