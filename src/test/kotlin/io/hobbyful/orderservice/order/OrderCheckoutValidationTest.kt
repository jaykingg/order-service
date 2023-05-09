package io.hobbyful.orderservice.order

import io.hobbyful.orderservice.fixtures.order
import io.hobbyful.orderservice.fixtures.storeCredit
import io.hobbyful.orderservice.fixtures.transactionView
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import javax.validation.Validation
import javax.validation.Validator

class OrderCheckoutValidationTest : BehaviorSpec({
    lateinit var validator: Validator

    beforeEach {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    Given("주문의 상태가 주문대기 일 때 유효성 검사") {
        When("배송정보와 결제정보가 존재하지 않을때") {
            val order = order {
                status = OrderStatus.PENDING
                shippingInfo = null
                payment = null
            }

            Then("유효성 검사 통과") {
                val violations = validator.validate(order)

                violations.shouldBeEmpty()
            }
        }
    }

    Given("주문의 상태가 결제요청 일 때 유효성 검사") {
        When("배송정보와 결제정보가 존재하지 않을때") {
            val order = order {
                storeCredit = storeCredit {
                    transaction = transactionView()
                }
                status = OrderStatus.CHECKOUT
                shippingInfo = null
                payment = null
            }

            Then("배송정보 유효성 검사 실패") {
                val violations = validator.validate(order)

                violations.size shouldBe 1
                violations.toString() shouldContain "shippingInfo"
            }
        }

        When("결제정보가 존재하지 않을때") {
            val order = order {
                storeCredit = storeCredit {
                    transaction = transactionView()
                }
                status = OrderStatus.CHECKOUT
                payment = null
            }

            Then("유효성 검사 통과") {
                val violations = validator.validate(order)

                violations.shouldBeEmpty()
            }
        }
    }

    Given("주문의 상태가 결제완료 일 때 유효성 검사") {
        When("배송정보와 결제정보가 존재하지 않을때") {
            val order = order {
                storeCredit = storeCredit {
                    transaction = transactionView()
                }
                status = OrderStatus.PAID
                shippingInfo = null
                payment = null
            }

            Then("배송정보 및 결제정보 유효성 검사 실패") {
                val violations = validator.validate(order)

                violations.size shouldBe 2
                violations.toString().run {
                    shouldContain("shippingInfo")
                    shouldContain("payment")
                }
            }
        }
    }
})
