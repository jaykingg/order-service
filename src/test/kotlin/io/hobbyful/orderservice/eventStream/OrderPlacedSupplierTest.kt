package io.hobbyful.orderservice.eventStream

import io.hobbyful.orderservice.eventStream.orderPlaced.OrderPlacedPayload
import io.hobbyful.orderservice.eventStream.orderPlaced.OrderPlacedSupplier
import io.hobbyful.orderservice.fixtures.order
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.messaging.converter.CompositeMessageConverter

@SpringBootTest
@Import(TestChannelBinderConfiguration::class)
class OrderPlacedSupplierTest(
    private val target: OutputDestination,
    private val converter: CompositeMessageConverter,
    private val orderPlacedSupplier: OrderPlacedSupplier
) : DescribeSpec({
    describe("send(Order)") {
        val order = order {
            id = ObjectId.get()
        }

        beforeEach {
            orderPlacedSupplier.send(order)
        }

        it("OK") {
            val sourceMessage = target.receive(3000, OrderPlacedSupplier.BINDING)
            val payload = converter.fromMessage(sourceMessage, OrderPlacedPayload::class.java) as OrderPlacedPayload

            payload.should {
                it.orderId shouldBe order.id
                it.customerId shouldBe order.customerId
                it.lineItems shouldBe order.lineItems
            }
        }
    }
})