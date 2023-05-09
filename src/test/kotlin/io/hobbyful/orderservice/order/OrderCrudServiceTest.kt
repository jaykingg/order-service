package io.hobbyful.orderservice.order

import com.ninjasquad.springmockk.MockkBean
import io.hobbyful.orderservice.fixtures.orderListView
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.springframework.context.annotation.Import
import java.util.*

@Import(OrderCrudService::class)
class OrderCrudServiceTest(
    @MockkBean private val orderRepository: OrderRepository,
    private val orderCrudService: OrderCrudService
) : DescribeSpec({

    beforeEach {
        clearMocks(orderRepository)
    }


    describe("getAllByCustomerIdAndPaid(customerId: String, criteria: OrderCriteria)") {
        val orders = listOf(orderListView())
        val customerId = UUID.randomUUID().toString()
        context("조회에 성공한 경우") {
            it("200 ok") {
                coEvery { orderRepository.findAllByCustomerIdAndPaymentIsNotNull(any(),any()) } returns orders.asFlow()
                val result = orderCrudService.getAllByCustomerIdAndPaid(customerId,OrderCriteria())
                result.toList() shouldBe orders
            }
        }
    }
})