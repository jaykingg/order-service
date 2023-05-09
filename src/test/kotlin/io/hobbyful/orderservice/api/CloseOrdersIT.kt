package io.hobbyful.orderservice.api

import io.hobbyful.orderservice.core.SecurityConstants
import io.hobbyful.orderservice.eventStream.orderClosed.OrderClosedSupplier
import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.payment
import io.hobbyful.orderservice.order.OrderRepository
import io.hobbyful.orderservice.order.OrderStatus
import io.hobbyful.orderservice.order.OrderUpdateView
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.messaging.converter.CompositeMessageConverter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody
import java.time.Instant

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class CloseOrdersIT(
    private val webTestClient: WebTestClient,
    private val orderRepository: OrderRepository,
    private val target: OutputDestination,
    private val converter: CompositeMessageConverter,
) : DescribeSpec({
    val endpoint = "/admin/orders/close"
    val authToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority(SecurityConstants.SERVICE_ADMIN))

    val orders = List(3) { index ->
        order {
            number = Instant.now().plusSeconds(index.toLong()).toString()
            status = OrderStatus.DELIVERED
            payment = payment()
        }
    }

    describe("배송완료된 모든 주문의 구매확정 처리") {
        lateinit var response: ResponseSpec

        beforeEach {
            orderRepository.saveAll(orders).collect()
            response = webTestClient
                .mutateWith(authToken)
                .post()
                .uri(endpoint)
                .exchange()
        }

        afterEach {
            orderRepository.deleteAll()
        }

        it("200 OK") {
            response
                .expectStatus().isOk
                .expectBody<OrderUpdateView>()
                .returnResult()
                .responseBody.shouldNotBeNull()
                .should {
                    it.totalCount shouldBe orders.size
                }
        }

        it("order-closed 메세지 전송") {
            val message = target.receive(3000, OrderClosedSupplier.BINDING)
            val payload = converter.fromMessage(message, OrderUpdateView::class.java) as OrderUpdateView

            payload.totalCount shouldBe orders.size
        }
    }
})
