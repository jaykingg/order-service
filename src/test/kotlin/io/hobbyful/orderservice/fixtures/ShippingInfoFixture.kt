package io.hobbyful.orderservice.fixtures

import io.hobbyful.orderservice.shippingInfo.ShippingInfo

/**
 * 배송정보 fixture
 */
inline fun shippingInfo(block: ShippingInfoFixtureBuilder.() -> Unit = {}) =
    ShippingInfoFixtureBuilder().apply(block).build()

class ShippingInfoFixtureBuilder {
    var recipient: String = faker.name.name()
    var primaryPhoneNumber: String = "01012345678"
    var secondaryPhoneNumber: String? = null
    var zipCode: String = faker.address.postcode()
    var line1: String = faker.random.randomString()
    var line2: String? = null
    var note: String? = null

    fun build() = ShippingInfo(
        recipient = recipient,
        primaryPhoneNumber = primaryPhoneNumber,
        secondaryPhoneNumber = secondaryPhoneNumber,
        zipCode = zipCode,
        line1 = line1,
        line2 = line2,
        note = note
    )
}
