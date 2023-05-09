package io.hobbyful.orderservice.payment

/**
 * 카드 정보
 */
data class CardInfo(
    /**
     * 결제 금액
     */
    val amount: Int,

    /**
     * 카드사 코드
     */
    val company: String,

    /**
     * 마스킹된 카드번호
     */
    val number: String,

    /**
     * 카드 종류
     */
    val cardType: String,

    /**
     * 카드의 소유자 타입
     */
    val ownerType: String
)
