package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.summary.Summary

/**
 * 주문 결제 요약 fixture
 */
inline fun summary(block: SummaryFixtureBuilder.() -> Unit = {}) =
    SummaryFixtureBuilder().apply(block).build()

class SummaryFixtureBuilder {
    var subtotal: Int = 10000
    var storeCreditAmount: Int = 0
    var couponAmount: Int = 0
    var shippingCost: Int = 0
    var total: Int = 0

    fun build() = Summary(
        subtotal = subtotal,
        storeCreditAmount = storeCreditAmount,
        couponAmount = couponAmount,
        shippingCost = shippingCost,
        total = total
    )
}
