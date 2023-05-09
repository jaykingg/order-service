package io.hobbyful.orderservice.storeCredit.transaction

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 거래 구분
 */
enum class TransactionType(val label: String) {
    @JsonProperty("deposit")
    DEPOSIT("적립"),

    @JsonProperty("charge")
    CHARGE("사용"),

    @JsonProperty("expire")
    EXPIRE("소멸")
}
