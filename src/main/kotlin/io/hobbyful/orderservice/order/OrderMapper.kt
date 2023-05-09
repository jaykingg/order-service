package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.lineitem.LineItem
import io.hobbyful.orderservice.lineitem.LineItemView
import io.hobbyful.orderservice.payment.Payment
import io.hobbyful.orderservice.payment.PaymentView
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper
abstract class OrderMapper {
    fun toView(order: Order): OrderView = order.run {
        OrderView(
            id = id!!,
            number = number,
            name = name,
            status = status,
            lineItems = lineItemView(lineItems),
            shippingInfo = shippingInfo,
            payment = payment?.run(::paymentView),
            summary = summary
        )
    }

    protected abstract fun lineItemView(lineItems: Collection<LineItem>): List<LineItemView>

    @Mappings(
        Mapping(target = "card", source = "card.company"),
        Mapping(target = "easyPay", source = "easyPay.provider"),
        Mapping(target = "bank", source = "transfer.bank")
    )
    protected abstract fun paymentView(payment: Payment): PaymentView
}
