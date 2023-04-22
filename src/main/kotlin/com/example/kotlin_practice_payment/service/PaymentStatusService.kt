package com.example.kotlin_practice_payment.service

import com.example.kotlin_practice_payment.OrderStatus
import com.example.kotlin_practice_payment.TransactionStatus
import com.example.kotlin_practice_payment.TransactionType
import com.example.kotlin_practice_payment.domain.Order
import com.example.kotlin_practice_payment.domain.OrderTransaction
import com.example.kotlin_practice_payment.exception.ErrorCode
import com.example.kotlin_practice_payment.exception.ErrorCode.*
import com.example.kotlin_practice_payment.exception.PaymentException
import com.example.kotlin_practice_payment.repository.OrderRepository
import com.example.kotlin_practice_payment.repository.OrderTransactionRepository
import com.example.kotlin_practice_payment.repository.PaymentUserRepository
import com.example.kotlin_practice_payment.util.generateOrderId
import com.example.kotlin_practice_payment.util.generateTransactionId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 결제의 요청 저장, 성공, 실패 저장
 */

@Service
class PaymentStatusService(
    private val paymentUserRepository: PaymentUserRepository,
    private val orderRepository: OrderRepository,
    private val orderTransactionRepository: OrderTransactionRepository
) {

    @Transactional
    fun savePayRequest(
        payUserId: String,
        amount: Long,
        orderTitle: String,
        merchantTransactionId: String
    ): Long {
        val paymentUser = paymentUserRepository.findByPayUserId(payUserId)
            ?: throw PaymentException(INVALID_REQUEST, "사용자 없음 : $payUserId")
        val order = orderRepository.save(
            Order(
                orderId = generateOrderId(),
                paymentUser = paymentUser,
                orderStatus = OrderStatus.CREATED,
                orderTitle = orderTitle,
                orderAmount = amount
            )
        )
        orderTransactionRepository.save(
            OrderTransaction(
                transactionId = generateTransactionId(),
                order = order,
                transactionType = TransactionType.PAYMENT,
                transactionStatus = TransactionStatus.RESERVE,
                transactionAmount = amount,
                merchantTransactionId = "",
                payMethodTransactionId = merchantTransactionId,
                description = orderTitle
            )
        )

        return order.id ?: throw PaymentException(INTERNAL_SERVER_ERROR)
    }

    fun saveAsSuccess(orderId: Long, payMethodTransactionId: String): Pair<String, LocalDateTime> {
        val order = getOrderByOrderId(orderId)
            .apply {
                orderStatus = OrderStatus.PAID
                paidAmount = orderAmount
            }
        val orderTransaction = getOrderTransactionByOrder(order).apply {
            transactionStatus = TransactionStatus.SUCCESS
            this.payMethodTransactionId = payMethodTransactionId
            transactedAt = LocalDateTime.now()
        }
        return Pair(
            orderTransaction.transactionId,
            orderTransaction.transactedAt ?: throw PaymentException(INTERNAL_SERVER_ERROR)
        )
    }

    fun saveAsFailure(orderId: Long, errorCode: ErrorCode) {
        val order = getOrderByOrderId(orderId)
            .apply {
                orderStatus = OrderStatus.FAILED
            }
        val orderTransaction = getOrderTransactionByOrder(order).apply {
            transactionStatus = TransactionStatus.FAILURE
            failureCode = errorCode.name
            description = errorCode.errorMessage
        }
    }

    private fun getOrderTransactionByOrder(order: Order) =
        orderTransactionRepository.findByOrderAndTransactionType(
            order = order,
            transactionType = TransactionType.PAYMENT
        ).first()

    private fun getOrderByOrderId(orderId: Long) =
        orderRepository.findById(orderId).orElseThrow { throw PaymentException(ORDER_NOT_FOUND) }

}