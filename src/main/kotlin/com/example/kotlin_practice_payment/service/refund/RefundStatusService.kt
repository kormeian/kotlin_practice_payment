package com.example.kotlin_practice_payment.service.refund

import com.example.kotlin_practice_payment.OrderStatus
import com.example.kotlin_practice_payment.TransactionStatus
import com.example.kotlin_practice_payment.TransactionType
import com.example.kotlin_practice_payment.domain.Order
import com.example.kotlin_practice_payment.domain.OrderTransaction
import com.example.kotlin_practice_payment.exception.ErrorCode
import com.example.kotlin_practice_payment.exception.PaymentException
import com.example.kotlin_practice_payment.repository.OrderRepository
import com.example.kotlin_practice_payment.repository.OrderTransactionRepository
import com.example.kotlin_practice_payment.util.generateRefundTransactionId
import jakarta.transaction.Transactional
import org.hibernate.internal.util.collections.CollectionHelper.listOf
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RefundStatusService(
    private val orderRepository: OrderRepository,
    private val orderTransactionRepository: OrderTransactionRepository
) {

    @Transactional
    fun saveRefundRequest(
        originalTransactionId: String, // 결제 사용자
        merchantRefundId: String,
        refundAmount: Long,
        refundReason: String,
    ): Long {
        // 결제(Order Transaction) 확인
        // 환불이 가능한지 확인
        // 환불 Transaction 저장

        val originalTransaction =
            this.orderTransactionRepository.findByTransactionId(originalTransactionId)
                ?: throw PaymentException(ErrorCode.ORDER_NOT_FOUND)

        val order = originalTransaction.order
        validationRefund(order, refundAmount) // 해당 orderTransaction 에 대해 환불 가능 여부 체크

        // 새로운 orderTransaction 생성
        return orderTransactionRepository.save(
            OrderTransaction(
                order = order,
                transactionId = generateRefundTransactionId(),
                transactionType = TransactionType.REFUND,
                transactionStatus = TransactionStatus.RESERVE,
                transactionAmount = refundAmount,
                merchantTransactionId = merchantRefundId,
                description = refundReason
            )
        ).id ?: throw PaymentException(ErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun validationRefund(order: Order, refundAmount: Long) {

        if (order.orderStatus !in listOf(OrderStatus.PAID, OrderStatus.PARTIAL_REFUNDED)) {
            throw PaymentException(ErrorCode.CANNOT_REFUND)
        }

        if (order.refundedAmount + refundAmount > order.paidAmount) {
            throw PaymentException(ErrorCode.EXCEED_REFUNDABLE_AMOUNT)
        }
    }

    @Transactional
    fun saveAsSuccess(
        refundTransactionId: Long, refundMethodTransactionId: String?
    ): Pair<String, LocalDateTime> {

        val orderTransaction =
            this.orderTransactionRepository.findById(refundTransactionId)
                .orElseThrow {
                    throw PaymentException(ErrorCode.INTERNAL_SERVER_ERROR)
                }.apply {
                    transactionStatus = TransactionStatus.SUCCESS
                    this.payMethodTransactionId = refundMethodTransactionId
                    transactedAt = LocalDateTime.now()
                }

        val order = orderTransaction.order
        // 부분 환불 가능
        val totalRefundedAmount = getTotalRefundedAmount(orderTransaction.order)

        order.apply {
            orderStatus = getNewOrderStatus(this, totalRefundedAmount)
            refundedAmount = totalRefundedAmount
        }
        return Pair(
            orderTransaction.transactionId,
            orderTransaction.transactedAt ?: throw PaymentException(ErrorCode.INTERNAL_SERVER_ERROR)
        )
    }

    private fun getNewOrderStatus(
        order: Order,
        totalRefundedAmount: Long
    ) = if (order.orderAmount == totalRefundedAmount) OrderStatus.REFUNDED
    else OrderStatus.PARTIAL_REFUNDED

    private fun getTotalRefundedAmount(order: Order): Long =
        orderTransactionRepository.findByOrderAndTransactionType(
            order, TransactionType.REFUND
        ).filter { it.transactionStatus == TransactionStatus.SUCCESS }
            .sumOf { it.transactionAmount }

    fun saveAsFailure(refundTxId: Long, errorCode: ErrorCode): Unit {
        orderTransactionRepository.findById(refundTxId)
            .orElseThrow { PaymentException(ErrorCode.INTERNAL_SERVER_ERROR) }
            .apply {
                transactionStatus = TransactionStatus.FAILURE
                failureCode = errorCode.name
                description = errorCode.errorMessage
            }
    }

    private fun getOrderTransactions(order: Order) =
        this.orderTransactionRepository.findByOrderAndTransactionType(
            order = order,
            transactionType = TransactionType.PAYMENT
        )

    private fun getOrderByOrderId(orderId: Long): Order = this.orderRepository.findById(orderId)
        .orElseThrow { PaymentException(ErrorCode.INTERNAL_SERVER_ERROR) }
}