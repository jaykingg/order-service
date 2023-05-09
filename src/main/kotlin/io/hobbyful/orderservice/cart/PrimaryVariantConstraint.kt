package io.hobbyful.orderservice.cart

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * 준비물별 필수 구매 상품 포함 여부를 확인합니다
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PrimaryVariantConstraintValidator::class])
annotation class PrimaryVariantConstraint(
    val message: String = "구매 필수 상품이 포함되지 않은 준비물이 있습니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
