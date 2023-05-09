package io.hobbyful.orderservice.cart

import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.lineitem.isPrimary
import io.hobbyful.orderservice.lineitem.productId
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PrimaryVariantConstraintValidator : ConstraintValidator<PrimaryVariantConstraint, Collection<LineItem>> {
    override fun isValid(value: Collection<LineItem>?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrEmpty()) return false
        return value.groupBy { it.productId }
            .values
            .all { items -> items.any(LineItem::isPrimary) }
    }
}
