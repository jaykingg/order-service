package io.hobbyful.orderservice.storeCredit

import io.hobbyful.orderservice.storeCredit.transaction.TransactionView
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange
import javax.validation.Valid

/**
 * 고객 적립금 사용
 */
@Validated
@Component
class StoreCreditChargeTemplate(
    private val apiClient: WebClient,
) {
    suspend fun charge(
        customerId: String,
        @Valid payload: ChargeStoreCreditPayload,
        responseHandler: suspend (ClientResponse) -> TransactionView
    ) = apiClient.post()
        .uri("/internal/membership/store-credit/account/{customerId}/charge", customerId)
        .bodyValue(payload)
        .awaitExchange(responseHandler)
}
