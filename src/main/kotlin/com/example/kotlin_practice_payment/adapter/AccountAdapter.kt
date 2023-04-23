package com.example.kotlin_practice_payment.adapter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDateTime

@FeignClient(name = "account-adapter", url = "http://localhost:8090")
interface AccountAdapter {
    @PostMapping("/transaction/use")
    fun useAccount(
        @RequestBody useBalanceRequest: UseBalanceRequest
    ): UseBalanceResponse

    @PostMapping("/transaction/cancel")
    fun cancelUseAccount(
        @RequestBody cancelBalanceRequest: CancelBalanceRequest
    ): CancelBalanceResponse
}

data class CancelBalanceResponse(
    val accountNumber: String,
    val transactionResultType: TransactionResultType,
    val amount: Long,
    val transactionId: String,
    val transactionAt: LocalDateTime
)

data class CancelBalanceRequest(
    val transactionId: String,
    val accountNumber: String,
    val amount: Long
)

data class UseBalanceResponse(
    val accountNumber: String,
    val transactionResultType: TransactionResultType,
    val transactionId: String,
    val amount: Long,
    val transactedAt: LocalDateTime
)

enum class TransactionResultType {
    SUCCESS, FAIL
}

data class UseBalanceRequest(
    val userId: Long,
    val accountNumber: String,
    val amount: Long
)
