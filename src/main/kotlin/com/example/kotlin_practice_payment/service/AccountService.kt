package com.example.kotlin_practice_payment.service

import com.example.kotlin_practice_payment.adapter.AccountAdapter
import com.example.kotlin_practice_payment.adapter.UseBalanceRequest
import com.example.kotlin_practice_payment.exception.ErrorCode
import com.example.kotlin_practice_payment.exception.PaymentException
import com.example.kotlin_practice_payment.repository.OrderRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountAdapter: AccountAdapter,
    private val orderRepository: OrderRepository
) {
    @Transactional
    fun useAccount(
        orderId: Long
    ): String {
        val order = orderRepository.findById(orderId).orElseThrow { throw PaymentException(ErrorCode.ORDER_NOT_FOUND) }
        return accountAdapter.useAccount(
            UseBalanceRequest(
                userId = order.paymentUser.accountUserId,
                accountNumber = order.paymentUser.accountNumber,
                amount = order.orderAmount
            )
        ).transactionId
    }
}