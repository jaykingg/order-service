package io.hobbyful.orderservice.order

import com.fasterxml.jackson.annotation.JsonView
import io.hobbyful.orderservice.core.Views
import io.hobbyful.orderservice.core.subject
import io.hobbyful.orderservice.order.checkout.CheckoutPayload
import io.hobbyful.orderservice.order.checkout.OrderCheckoutService
import io.hobbyful.orderservice.order.refund.OrderRefundService
import io.hobbyful.orderservice.order.refund.RefundPayload
import io.hobbyful.orderservice.order.register.ItemsPayload
import io.hobbyful.orderservice.order.register.OrderRegisterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springdoc.api.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Order")
@SecurityRequirement(name = "aegis")
@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderCrudService: OrderCrudService,
    private val orderCheckoutService: OrderCheckoutService,
    private val orderRegisterService: OrderRegisterService,
    private val orderRefundService: OrderRefundService,
    private val mapper: OrderMapper
) {
    /**
     * 고객이 결제한 모든 주문을 조회합니다.
     */
    @Operation(summary = "결제한 모든 주문 조회")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @JsonView(Views.External::class)
    fun getOrders(
        token: BearerTokenAuthentication,
        @Valid @ParameterObject
        query: OrderCriteria
    ): Flow<OrderListView> = orderCrudService.getAllByCustomerIdAndPaid(token.subject, query)

    /**
     * 주문 Id 기반 주문 상세 정보를 조회합니다.
     *
     * @param orderId 주문 Id
     */
    @Operation(summary = "주문 상세 정보 조회")
    @GetMapping("/{orderId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @JsonView(Views.External::class)
    suspend fun getOrderById(
        token: BearerTokenAuthentication,
        @PathVariable orderId: ObjectId
    ): OrderView = orderCrudService.getByIdAndCustomerId(orderId, token.subject).run(mapper::toView)

    /**
     * 고객의 장바구니에서 구매 선택된 품목을 토대로 새로운 주문을 생성합니다.
     */
    @Operation(summary = "장바구니 기준 주문 생성")
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.External::class)
    suspend fun registerOrderByCart(token: BearerTokenAuthentication): OrderView =
        orderRegisterService.registerByCart(token.subject).run(mapper::toView)

    /**
     * 준비물 화면에서 선택된 품목을 토대로 새로운 주문을 생성합니다.
     */
    @Operation(summary = "바로 구매하기 기반 주문 생성")
    @PostMapping("/buy-now", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.External::class)
    suspend fun registerOrderByPurchase(
        token: BearerTokenAuthentication,
        @RequestBody
        payload: ItemsPayload
    ): OrderView = orderRegisterService.registerByPurchase(token.subject, payload).run(mapper::toView)

    /**
     * 주문 결제에 필요한 배송지, 적립금 사용, 쿠폰 사용 정보를 추가하고 `결제요청` 상태로 변경합니다.
     *
     * @param orderId 주문 Id
     */
    @Operation(summary = "주문 결제 요청")
    @PostMapping("/{orderId}/checkout", produces = [MediaType.APPLICATION_JSON_VALUE])
    @JsonView(Views.External::class)
    suspend fun checkoutOrder(
        token: BearerTokenAuthentication,
        @PathVariable orderId: ObjectId,
        @Valid @RequestBody
        payload: CheckoutPayload
    ): OrderView = orderCheckoutService.checkout(orderId, token.subject, payload).run(mapper::toView)

    /**
     * 결제완료 상태의 주문을 취소하고 결제 금액을 모두 환불합니다.
     *
     * @param orderId 주문 Id
     */
    @Operation(summary = "주문 결제 취소")
    @PostMapping("/{orderId}/refund")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun refundOrder(
        token: BearerTokenAuthentication,
        @PathVariable orderId: ObjectId,
        @Valid @RequestBody
        payload: RefundPayload
    ) {
        orderRefundService.refund(orderId, token.subject, payload)
    }
}
