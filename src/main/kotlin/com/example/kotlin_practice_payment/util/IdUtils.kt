package com.example.kotlin_practice_payment.util

import java.util.*


fun generateOrderId() = "PO" + generateUUID()
fun generateTransactionId() = "PT" + generateUUID()
fun generateRefundTransactionId() = "RT" + generateUUID()

private fun generateUUID() = UUID.randomUUID().toString().replace("-", "")