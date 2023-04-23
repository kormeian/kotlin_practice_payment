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
}

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
