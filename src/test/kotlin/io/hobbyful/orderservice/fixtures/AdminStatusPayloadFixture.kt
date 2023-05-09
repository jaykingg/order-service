package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.order.AdminStatusPayload
import java.time.Instant

inline fun adminStatusPayload(block: AdminStatusPayloadFixture.() -> Unit = {}) =
    AdminStatusPayloadFixture().apply(block).build()

class AdminStatusPayloadFixture {
    var numbers: Set<String> =
        setOf(Instant.now().toEpochMilli().toString(), Instant.now().plusSeconds(1).toEpochMilli().toString())

    fun build() = AdminStatusPayload(
        numbers = numbers,
    )
}