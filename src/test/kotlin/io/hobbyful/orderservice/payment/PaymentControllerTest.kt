package io.hobbyful.orderservice.payment

import com.ninjasquad.springmockk.MockkBean
import io.hobbyful.orderservice.fixtures.order
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@Import(TestChannelBinderConfiguration::class)
class PaymentControllerTest(
    @MockkBean private val paymentService: PaymentService,
    private val webClient: WebTestClient
) : BehaviorSpec({
    val endpoint = "/orders/payment/success"

    Given("GET $endpoint") {
        When("결제 승인에 성공했을때") {
            val request = webClient.get().uri { uriBuilder ->
                uriBuilder
                    .path(endpoint)
                    .queryParam("paymentKey", "paymentKey")
                    .queryParam("orderId", ObjectId.get())
                    .queryParam("amount", 15_000)
                    .build()
            }

            Then("204 No Content") {
                coEvery { paymentService.confirmPayment(any()) } returns order()

                request
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }
})
