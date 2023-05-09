package io.hobbyful.orderservice.order

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 주문을 생성한 스토어
 */
enum class StoreType(val code: String) {
    @field: JsonProperty("hobbyful")
    HOBBYFUL("HF")
}
