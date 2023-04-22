package com.example.kotlin_practice_payment.service

import com.example.kotlin_practice_payment.exception.ErrorCode
import com.example.kotlin_practice_payment.exception.PaymentException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal class PaymentServiceTest {
    @RelaxedMockK
    lateinit var paymentStatusService: PaymentStatusService

    @MockK
    lateinit var accountService: AccountService

    @InjectMockKs
    lateinit var paymentService: PaymentService

    @Test
    fun `결제 성공`() {
        //given
        val request = PayServiceRequest(
            payUserId = "payUserId",
            amount = 100,
            merchantTransactionId = "merchantTransactionId",
            orderTitle = "orderTitle"
        )
        every {
            paymentStatusService.savePayRequest(any(), any(), any(), any())
        } returns 1
        every {
            accountService.useAccount(any())
        } returns "payMethodTransactionId"
        every {
            paymentStatusService.saveAsSuccess(any(), any())
        } returns Pair("transactionId", LocalDateTime.now())

        //when
        val result = paymentService.pay(request)

        //then
        result.amount shouldBe 100
        verify(exactly = 1) {
            paymentStatusService.savePayRequest(any(), any(), any(), any())
            accountService.useAccount(any())
            paymentStatusService.saveAsSuccess(any(), any())
        }
        verify(exactly = 0) {
            paymentStatusService.saveAsFailure(any(), any())
        }
    }

    @Test
    fun `결제 실패 - 잔액 부족`() {
        //given
        val request = PayServiceRequest(
            payUserId = "payUserId",
            amount = 100,
            merchantTransactionId = "merchantTransactionId",
            orderTitle = "orderTitle"
        )
        every {
            paymentStatusService.savePayRequest(any(), any(), any(), any())
        } returns 1
        every {
            accountService.useAccount(any())
        } throws PaymentException(ErrorCode.LACK_BALANCE)

        //when
        val result = shouldThrow<PaymentException> {
            paymentService.pay(request)
        }

        //then
        result.errorCode shouldBe ErrorCode.LACK_BALANCE
        verify(exactly = 1) {
            paymentStatusService.saveAsFailure(any(), any())
        }
        verify(exactly = 0) {
            paymentStatusService.saveAsSuccess(any(), any())
        }
    }
}