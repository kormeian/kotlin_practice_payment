package com.example.kotlin_practice_payment.service.refund

import com.example.kotlin_practice_payment.exception.ErrorCode
import com.example.kotlin_practice_payment.exception.PaymentException
import com.example.kotlin_practice_payment.service.AccountService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class RefundServiceTest : BehaviorSpec({
    val refundStatusService = mockk<RefundStatusService>(relaxed = true)
    val accountService = mockk<AccountService>(relaxed = true)
    val refundService = RefundService(refundStatusService, accountService)

    Given("환불 요청이 정상적으로 저장됨") {
        val request = RefundServiceRequest(
            transactionId = "originalTransactionId",
            refundId = "merchantRefundId",
            refundAmount = 100,
            refundReason = "refundReason"
        )
        every {
            refundStatusService.saveRefundRequest(any(), any(), any(), any())
        } returns 1L
        When("계좌 시스템이 정상적으로 환불") {
            every {
                accountService.cancelUseAccount(any())
            } returns "accountTxId"
            every {
                refundStatusService.saveAsSuccess(any(), any())
            } returns Pair("refundTxId", LocalDateTime.MIN)

            val result = refundService.refund(request)

            Then("트랜잭션ID, 금액이 응답으로 온다.") {
                result.refundTransactionId shouldBe "refundTxId"
                result.refundAmount shouldBe 100
            }

            Then("saveAssuccess 호출, saveAsFailure 호출 안됨") {
                verify(exactly = 1) {
                    refundStatusService.saveAsSuccess(any(), any())
                }
                verify(exactly = 0) {
                    refundStatusService.saveAsFailure(any(), any())
                }
            }
        }

        When("계좌 시스템이 환불 실패") {
            every {
                accountService.cancelUseAccount(any())
            } throws PaymentException(ErrorCode.EXCEED_REFUNDABLE_AMOUNT)
            val result = shouldThrow<PaymentException> {
                refundService.refund(request)
            }

            Then("환불 실패로 저장") {
                result.errorCode shouldBe ErrorCode.EXCEED_REFUNDABLE_AMOUNT
                verify(exactly = 1) {
                    refundStatusService.saveAsFailure(any(), any())
                }
            }
        }
    }
})
