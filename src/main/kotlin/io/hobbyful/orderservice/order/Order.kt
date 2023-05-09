package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.lineitem.firstProductName
import io.hobbyful.orderservice.lineitem.subtotal
import io.hobbyful.orderservice.order.refund.Refund
import io.hobbyful.orderservice.payment.Payment
import io.hobbyful.orderservice.shippingInfo.ShippingInfo
import io.hobbyful.orderservice.storeCredit.StoreCredit
import io.hobbyful.orderservice.summary.Summary
import io.hobbyful.orderservice.summary.Summary.Companion.summary
import org.bson.types.ObjectId
import org.hibernate.validator.group.GroupSequenceProvider
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.Instant
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * 주문
 */
@Document
@GroupSequenceProvider(OrderGroupSequenceProvider::class)
data class Order(
    /**
     * 주문 ID
     */
    @MongoId
    val id: ObjectId? = null,

    /**
     * 주문 번호
     */
    @Indexed(direction = IndexDirection.DESCENDING, unique = true)
    @field: NotBlank
    val number: String,

    /**
     * 고객 ID
     */
    @Indexed
    @field: NotBlank
    val customerId: String,

    /**
     * 장바구니 ID
     */
    val cartId: ObjectId?,

    /**
     * 주문명
     */
    @field: NotBlank
    val name: String,

    /**
     * 적립금 거래 내역
     */
    @field: Valid
    val storeCredit: StoreCredit,

    /**
     * 주문을 생성한 스토어
     */
    val store: StoreType,

    /**
     * 주문 상태
     */
    val status: OrderStatus,

    /**
     * 구매 선택된 주문품목 리스트
     */
    @field: NotEmpty
    @field: Valid
    val lineItems: List<LineItem>,

    /**
     * 배송정보
     */
    @field: NotNull(groups = [OnCheckoutStatus::class])
    @field: Valid
    val shippingInfo: ShippingInfo?,

    /**
     * 결제정보
     */
    @field: NotNull(groups = [OnPaidStatus::class])
    @field: Valid
    val payment: Payment?,

    /**
     * 취소정보
     */
    @field: NotNull(groups = [OnRefundedStatus::class])
    @field: Valid
    val refund: Refund?,

    /**
     * 주문 결제 정산 요약
     */
    @field: Valid
    val summary: Summary,

    /**
     * 구매 적립금 지급 여부
     */
    val rewarded: Boolean,

    /**
     * 주문 완료 일시
     */
    @field: NotNull(groups = [OnPaidStatus::class])
    val placedAt: Instant?,

    /**
     * 주문마감(관리자 승인) 일시
     */
    @field: NotNull(groups = [OnPlanningStatus::class])
    val approvedAt: Instant?,

    /**
     * 구매확정 일시
     */
    @field: NotNull(groups = [OnClosedStatus::class])
    val closedAt: Instant?,

    /**
     * 최초 주문 생성일
     */
    @CreatedDate
    val createdAt: Instant? = null,

    /**
     * 최근 주문 수정일
     */
    @LastModifiedDate
    val updatedAt: Instant? = null
) {
    companion object {
        /**
         * 주문 생성 dsl
         */
        inline fun order(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var customerId: String = ""
        var cartId: ObjectId? = null
        var store: StoreType = StoreType.HOBBYFUL
        var status: OrderStatus = OrderStatus.PENDING
        var lineItems: List<LineItem> = emptyList()
        var rewarded: Boolean = false
        var placedAt: Instant? = null
        var approvedAt: Instant? = null
        var closedAt: Instant? = null

        private val number
            get() = "${store.code}${Instant.now().toEpochMilli()}"

        /**
         * 주문명 생성
         *
         * @param productName 준비물 이름
         * @param itemCount 총 상품 개수
         */
        private fun getOrderName(productName: String, itemCount: Int): String =
            if (itemCount > 1) "$productName 외 ${itemCount - 1}개" else productName

        fun build() = Order(
            number = number,
            customerId = customerId,
            cartId = cartId,
            name = getOrderName(lineItems.firstProductName, lineItems.size),
            storeCredit = StoreCredit.of(0),
            store = store,
            status = status,
            lineItems = lineItems,
            shippingInfo = null,
            payment = null,
            refund = null,
            summary = summary {
                subtotal = lineItems.subtotal
                storeCreditAmount = 0
            },
            rewarded = rewarded,
            placedAt = placedAt,
            approvedAt = approvedAt,
            closedAt = closedAt
        )
    }
}
