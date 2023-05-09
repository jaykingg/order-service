package io.hobbyful.orderservice.order

import com.fasterxml.jackson.annotation.JsonView
import io.hobbyful.orderservice.base.RestAdminController
import io.hobbyful.orderservice.core.PagedView
import io.hobbyful.orderservice.core.Views
import io.hobbyful.orderservice.core.toPagedView
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.flow.Flow
import org.springdoc.api.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@RestAdminController
class OrderAdminController(
    private val orderQueryService: OrderQueryService,
    private val orderUpdateService: OrderUpdateService
) {
    /**
     * 필터 기반 전체 주문을 조회합니다.
     */
    @Operation(summary = "전체 주문 조회")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @JsonView(Views.Internal::class)
    suspend fun allFilteredOrders(
        token: BearerTokenAuthentication,
        @Valid @ParameterObject
        adminFilterCriteria: AdminFilterCriteria,
    ): PagedView<Order> = orderQueryService.getAllFiltered(adminFilterCriteria).toPagedView()

    /**
     * 주어진 키워드로 모든 주문을 검색합니다.
     */
    @Operation(summary = "주문 통합 검색")
    @GetMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @JsonView(Views.Internal::class)
    suspend fun searchOrders(
        token: BearerTokenAuthentication,
        @Valid @ParameterObject
        criteria: AdminSearchCriteria
    ): Flow<Order> = orderQueryService.searchAll(criteria)

    /**
     * 모든 `결제완료` 주문들을 일괄적으로 마감합니다.
     */
    @Operation(summary = "결제완료 주문 일괄 마감")
    @PostMapping("/approve", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun approveAllPaidOrders(
        token: BearerTokenAuthentication
    ): OrderUpdateView = orderUpdateService.approveAllPaidOrders()

    @Operation(summary = "일괄 구매확정 처리")
    @PostMapping("/close", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun closeAllDeliveredOrders(): OrderUpdateView = orderUpdateService.closeAllDeliveredOrders()

    /**
     * 출력을 위한 주문을 조회합니다.
     */
    @Operation(summary = "주문 출력을 위한 조회")
    @GetMapping("/export", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun exportOrders(
        token: BearerTokenAuthentication,
        @Valid @ParameterObject
        criteria: AdminExportCriteria,
    ): Flow<OrderExport> = orderQueryService.getAllToExport(criteria)

    /**
     * 주문 상태를 배송 중 또는 배송완료로 변경합니다.
     */
    @Operation(summary = "주문 상태를 배송 중 또는 배송완료로 변경")
    @PostMapping("/status/{targetStatus}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun changeOrderStatus(
        token: BearerTokenAuthentication,
        @PathVariable targetStatus: OrderStatus,
        @Valid @RequestBody
        payload: AdminStatusPayload
    ): OrderUpdateView = orderUpdateService.changeOrderStatus(payload, targetStatus)
}
