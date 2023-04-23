package com.example.kotlin_practice_payment.controller

import com.example.kotlin_practice_payment.service.refund.RefundService
import com.example.kotlin_practice_payment.service.refund.RefundServiceRequest
import com.example.kotlin_practice_payment.service.refund.RefundServiceResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RequestMapping("/api/v1")
@RestController
class RefundController(
    private val refundService: RefundService
) {
    @PostMapping("/refund")
    fun refund(
        @Valid @RequestBody
        refundRequest: RefundRequest

    ): RefundResponse = RefundResponse.from(
        this.refundService.refund(
            refundRequest.toRefundServiceRequest()
        )
    )
}

/*
    환불시에는 원결제의 transactionId 를 받아야 함
 */
data class RefundRequest(
    val transactionId: String,
    val refundId: String,
    val refundAmount: Long,
    val refundReason: String,

    ) {

    // RefundRequest -> RefundServiceRequest
    fun toRefundServiceRequest() = RefundServiceRequest(
        transactionId = this.transactionId,
        refundId = this.refundId,
        refundAmount = this.refundAmount,
        refundReason = this.refundReason
    )
}

data class RefundResponse(
    val refundTransactionId: String,
    val refundAmount: Long,
    val refundAt: LocalDateTime,
) {
    companion object {
        // RefundServiceResponse -> RefundResponse
        fun from(refundServiceResponse: RefundServiceResponse) =
            RefundResponse(
                refundTransactionId = refundServiceResponse.refundTransactionId,
                refundAmount = refundServiceResponse.refundAmount,
                refundAt = refundServiceResponse.refundedAt
            )
    }
}