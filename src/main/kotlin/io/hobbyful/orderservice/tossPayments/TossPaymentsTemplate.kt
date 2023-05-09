package io.hobbyful.orderservice.tossPayments

import io.hobbyful.orderservice.payment.Payment
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange
import javax.validation.Valid
import javax.validation.constraints.NotBlank

/**
 * 토스페이먼츠 API 클라이언트
 */
@Validated
@Component
class TossPaymentsTemplate(private val tossPaymentsClient: WebClient) {
    /**
     * 결제 승인 요청 API 호출
     *
     * 참고 - [코어 API > 결제 승인](https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8)
     *
     * @param parameters 결제 승인 Request Parameters
     * @param responseHandler 결제 응답 핸들러
     */
    suspend fun confirmPayment(
        @Valid parameters: ConfirmPaymentParameters,
        responseHandler: suspend (ClientResponse) -> Payment
    ) = tossPaymentsClient.post()
        .uri("/payments/confirm")
        .bodyValue(parameters)
        .awaitExchange(responseHandler)

    /**
     * 결제 취소 API 호출
     *
     * **참고**:
     *
     * @param paymentKey 결제 건에 대한 고유 Id
     * @param payload 결제 취소 Request Payload
     * @param responseHandler 결제 취소 응답 핸들러
     */
    suspend fun cancelPayment(
        @NotBlank paymentKey: String,
        @Valid payload: CancelPaymentPayload,
        responseHandler: suspend (ClientResponse) -> Payment
    ) = tossPaymentsClient.post()
        .uri("/payments/{paymentKey}/cancel", paymentKey)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .awaitExchange(responseHandler)
}
