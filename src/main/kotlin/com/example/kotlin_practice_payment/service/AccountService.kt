package com.example.kotlin_practice_payment.service

import com.example.kotlin_practice_payment.TransactionType
import com.example.kotlin_practice_payment.adapter.AccountAdapter
import com.example.kotlin_practice_payment.adapter.CancelBalanceRequest
import com.example.kotlin_practice_payment.adapter.UseBalanceRequest
import com.example.kotlin_practice_payment.exception.ErrorCode
import com.example.kotlin_practice_payment.exception.PaymentException
import com.example.kotlin_practice_payment.repository.OrderRepository
import com.example.kotlin_practice_payment.repository.OrderTransactionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountAdapter: AccountAdapter,
    private val orderRepository: OrderRepository,
    private val orderTransactionRepository: OrderTransactionRepository,
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

    @Transactional
    fun cancelUseAccount(refundTxId: Long): String {
        val refundTransaction = orderTransactionRepository
            .findById(refundTxId)
            .orElseThrow {
                PaymentException(ErrorCode.INTERNAL_SERVER_ERROR)
            }

        val order = refundTransaction.order
        val paymentTransaction =
            orderTransactionRepository.findByOrderAndTransactionType(
                order, TransactionType.PAYMENT
            ).first()

        return accountAdapter.cancelUseAccount(
            CancelBalanceRequest(
                transactionId = paymentTransaction.payMethodTransactionId
                    ?: throw PaymentException(ErrorCode.INTERNAL_SERVER_ERROR),
                accountNumber = order.paymentUser.accountNumber,
                amount = refundTransaction.transactionAmount
            )
        ).transactionId
    }
}