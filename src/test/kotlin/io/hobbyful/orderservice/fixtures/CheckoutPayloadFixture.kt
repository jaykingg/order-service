package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.order.checkout.CheckoutPayload
import io.hobbyful.orderservice.shippingInfo.ShippingInfo

/**
 * 주문 결제 요청 payload fixture
 */
inline fun checkoutPayload(block: CheckoutPayloadFixtureBuilder.() -> Unit = {}) =
    CheckoutPayloadFixtureBuilder().apply(block).build()

class CheckoutPayloadFixtureBuilder {
    var shippingInfo: ShippingInfo = shippingInfo()
    var storeCreditAmount: Int = 1000

    fun build() = CheckoutPayload(
        shippingInfo = shippingInfo,
        storeCreditAmount = storeCreditAmount
    )
}
