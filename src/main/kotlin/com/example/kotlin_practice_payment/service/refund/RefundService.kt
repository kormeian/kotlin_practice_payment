package com.example.kotlin_practice_payment.service.refund

import com.example.kotlin_practice_payment.exception.ErrorCode
import com.example.kotlin_practice_payment.exception.PaymentException
import com.example.kotlin_practice_payment.service.AccountService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RefundService(
    private val refundStatusService: RefundStatusService,
    private val accountService: AccountService,
) {

    fun refund(
        request: RefundServiceRequest
    ): RefundServiceResponse {

        val refundTxId = refundStatusService.saveRefundRequest(
            originalTransactionId = request.transactionId,
            merchantRefundId = request.refundId,
            refundAmount = request.refundAmount,
            refundReason = request.refundReason
        )

        return try {
            val refundAccountTransactionId = accountService.cancelUseAccount(refundTxId)

            val (transactionId, transactionAt) =
                refundStatusService.saveAsSuccess(refundTxId, refundAccountTransactionId)

            RefundServiceResponse(
                refundTransactionId = transactionId,
                refundAmount = request.refundAmount,
                refundedAt = transactionAt
            )
        } catch (e: Exception) {
            refundStatusService.saveAsFailure(refundTxId, getErrorCode(e))
            throw e
        }
    }

    private fun getErrorCode(e: Exception) = if (e is PaymentException) e.errorCode
    else ErrorCode.INTERNAL_SERVER_ERROR
}

data class RefundServiceRequest(
    val transactionId: String,
    val refundId: String,
    val refundAmount: Long,
    val refundReason: String
)

class RefundServiceResponse(
    val refundTransactionId: String,
    val refundAmount: Long,
    val refundedAt: LocalDateTime
)