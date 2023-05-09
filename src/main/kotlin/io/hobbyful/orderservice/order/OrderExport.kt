package io.hobbyful.orderservice.order

import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

data class OrderExport(
    /**
     * 주문번호
     */
    val number: String,

    /**
     * 고객 Id
     */
    val customerId: String,

    /**
     * 스토어 구분
     */
    val store: StoreType,

    /**
     * 주문상태
     */
    val status: OrderStatus,

    /**
     * 브랜드 이름
     */
    @Field("lineItems.product.brand.name")
    val brandName: String,

    /**
     * 준비물 이름
     */
    @Field("lineItems.product.name")
    val productName: String,

    /**
     * 상품 이름
     */
    @Field("lineItems.variant.name")
    val variantName: String,

    /**
     * 재고관리코드(SKU)
     */
    @Field("lineItems.variant.sku")
    val sku: String,

    /**
     * 상품정가
     */
    @Field("lineItems.variant.basePrice")
    val basePrice: Int,

    /**
     * 상품판매가
     */
    @Field("lineItems.variant.price")
    val price: Int,

    /**
     * 구매수량
     */
    @Field("lineItems.quantity")
    val quantity: Int,

    /**
     * 필수상품 여부
     */
    @Field("lineItems.variant.primary")
    val primary: Boolean,

    /**
     * 수령인
     */
    @Field("shippingInfo.recipient")
    val recipient: String,

    /**
     * 연락처1
     */
    @Field("shippingInfo.primaryPhoneNumber")
    val primaryPhoneNumber: String,

    /**
     * 연락처2
     */
    @Field("shippingInfo.secondaryPhoneNumber")
    val secondaryPhoneNumber: String?,

    /**
     * 우편변호
     */
    @Field("shippingInfo.zipCode")
    val zipCode: String,

    /**
     * 주소1
     */
    @Field("shippingInfo.line1")
    val line1: String,

    /**
     * 주소2
     */
    @Field("shippingInfo.line2")
    val line2: String?,

    /**
     * 배송요청사항
     */
    @Field("shippingInfo.note")
    val note: String?,

    /**
     * 결제수단
     */
    @Field("payment.method")
    val method: String?,

    /**
     * 간편 결제사
     */
    @Field("payment.easyPay.provider")
    val easyPayProvider: String?,

    /**
     * 카드사
     */
    @Field("payment.card.company")
    val cardCompany: String?,

    /**
     * 계좌이체 은행
     */
    @Field("payment.transfer.bank")
    val transferBank: String?,

    /**
     * 주문총액
     */
    @Field("summary.subtotal")
    val subtotal: Int,

    /**
     * 적립금 사용액
     */
    @Field("summary.storeCreditAmount")
    val storeCreditAmount: Int,

    /**
     * 배송비
     */
    @Field("summary.shippingCost")
    val shippingCost: Int,

    /**
     * 결제총액
     */
    @Field("summary.total")
    val total: Int,

    /**
     * 정산율
     */
    @Field("lineItems.product.brandAdjustmentRate")
    val brandAdjustmentRate: Double,

    /**
     * 결제 승인일
     */
    val placedAt: Instant?,

    /**
     * 주문 마감일
     */
    val approvedAt: Instant?
)
