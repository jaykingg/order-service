package io.hobbyful.orderservice.order

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider

/**
 * Hibernate Validator SPI for the dynamic redefinition of default group sequence
 *
 * 참고: [Hibernate Validator > @GroupSequenceProvider](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#_groupsequenceprovider)
 */
class OrderGroupSequenceProvider : DefaultGroupSequenceProvider<Order> {
    override fun getValidationGroups(order: Order?): MutableList<Class<*>> {
        val groupSequence = mutableListOf<Class<*>>(Order::class.java)

        if (order == null || order.isPendingStatus) return groupSequence

        if (order.isCheckoutStatus) groupSequence.add(OnCheckoutStatus::class.java)
        if (order.isPaidStatus) groupSequence.add(OnPaidStatus::class.java)
        if (order.isPlanningStatus) groupSequence.add(OnPlanningStatus::class.java)
        if (order.isClosedStatus) groupSequence.add(OnClosedStatus::class.java)
        if (order.isCancelledStatus) groupSequence.add(OnRefundedStatus::class.java)

        return groupSequence
    }
}
