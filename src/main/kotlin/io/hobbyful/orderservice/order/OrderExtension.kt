package io.hobbyful.orderservice.order

/**
 * 주문 총액
 */
val Order.total: Int
    get() = summary.total

val Order.isPendingStatus: Boolean
    get() = status == OrderStatus.PENDING

val Order.isCheckoutStatus: Boolean
    get() = status == OrderStatus.CHECKOUT

val Order.isPaidStatus: Boolean
    get() = status == OrderStatus.PAID

val Order.isPlanningStatus: Boolean
    get() = status == OrderStatus.PLANNING

val Order.isClosedStatus: Boolean
    get() = status == OrderStatus.CLOSED

val Order.isCancelledStatus: Boolean
    get() = status == OrderStatus.CANCELLED

val Order.isNotPendingStatus: Boolean
    get() = !isPendingStatus

val Order.isNotCheckoutStatus: Boolean
    get() = !isCheckoutStatus

val Order.isNotPaidStatus: Boolean
    get() = !isPaidStatus

val Order.isNotPlanning: Boolean
    get() = !isPlanningStatus
