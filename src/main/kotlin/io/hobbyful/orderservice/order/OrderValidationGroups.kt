package io.hobbyful.orderservice.order

/**
 * 결제요청 주문에 대한 유효성 검사 그룹
 */
interface OnCheckoutStatus

/**
 * 결제완료 주문에 대한 유효성 검사 그룹
 */
interface OnPaidStatus : OnCheckoutStatus

/**
 * 취소완료 주문에 대한 유효성 검사 그룹
 */
interface OnRefundedStatus : OnPaidStatus

/**
 * 배송준비중 주문에 대한 유효성 검사 그룹
 */
interface OnPlanningStatus : OnPaidStatus

/**
 * 구매확정된 주문에 대한 유효성 검사 그룹
 */
interface OnClosedStatus : OnPlanningStatus
