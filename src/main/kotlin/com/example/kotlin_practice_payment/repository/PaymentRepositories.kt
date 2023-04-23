package com.example.kotlin_practice_payment.repository

import com.example.kotlin_practice_payment.TransactionType
import com.example.kotlin_practice_payment.domain.Order
import com.example.kotlin_practice_payment.domain.OrderTransaction
import com.example.kotlin_practice_payment.domain.PaymentUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentUserRepository : JpaRepository<PaymentUser, Long> {
    fun findByPayUserId(payUserId: String): PaymentUser?
}

interface OrderRepository : JpaRepository<Order, Long>

interface OrderTransactionRepository : JpaRepository<OrderTransaction, Long> {
    fun findByOrderAndTransactionType(
        order: Order,
        transactionType: TransactionType
    ): List<OrderTransaction>

    fun findByTransactionId(transactionId: String): OrderTransaction?
}