package io.hobbyful.orderservice.core

interface Views {
    /**
     * Public client 전용
     */
    interface External

    /**
     * Admin, Messaging 등 내부 client 전용
     */
    interface Internal : External
}
