package com.example.kotlin_practice_payment

enum class OrderStatus {
    CREATED,
    PAID,
    CANCELED,
    FAILED,
    PARTIAL_REFUNDED,
    REFUNDED
}

enum class TransactionStatus {
    RESERVE,
    SUCCESS,
    FAILURE
}

enum class TransactionType {
    PAYMENT,
    REFUND,
    CANCEL
}
