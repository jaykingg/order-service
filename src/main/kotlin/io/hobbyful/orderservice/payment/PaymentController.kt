package io.hobbyful.orderservice.payment

import io.hobbyful.orderservice.tossPayments.ConfirmPaymentParameters
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springdoc.api.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Payment")
@RestController
@RequestMapping("/orders/payment")
class PaymentController(
    private val paymentService: PaymentService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 토스페이먼츠의 결제 요청 성공시 사용되는 Redirect URL
     *
     * 전달받은 쿼리 파라미터를 토대로 토스페이먼츠에 결제 승인을 요청합니다
     */
    @Operation(summary = "토스페이먼츠 결제 승인")
    @GetMapping("/success")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun paymentSuccessCallback(
        @Valid @ParameterObject
        requestParams: ConfirmPaymentParameters
    ) {
        paymentService.confirmPayment(requestParams)
    }

    /**
     * 토스페이먼츠에서 전달받는 webhook payload 를 토대로 주문과 주문의 결제 상태를 변경합니다.
     */
    @Operation(summary = "토스페이먼츠 Webhook Listener")
    @PostMapping("/toss-webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun paymentRefundWebhookCallback(
        @Valid @RequestBody
        payload: TossWebhookPayload
    ) {
        logger.info("TossPayments Webhook - {}", payload)
        paymentService.cancelByWebhook(payload)
    }
}
