package com.example.kotlin_practice_payment.controller

import com.example.kotlin_practice_payment.service.payment.PayServiceResponse
import com.example.kotlin_practice_payment.service.payment.PaymentService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime


@WebMvcTest(PaymentController::class)
internal class PaymentControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockkBean
    private lateinit var paymentService: PaymentService

    private val mapper = ObjectMapper()

    @Test
    @DisplayName("결제 요청 - 성공 응답")
    fun `결제 요청 - 성공 응답`() {
        //given
        every {
            paymentService.pay(any())
        } returns PayServiceResponse(
            payUserId = "paymentUserId",
            amount = 100,
            transactionId = "transactionId",
            transactedAt = LocalDateTime.MIN
        )

        //when


        //then
        mockMvc.post("/api/v1/pay") {
            headers {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
            }
            content = mapper.writeValueAsString(
                PayRequest(payUserId = "p1", amount = 100, merchantTransactionId = "m1", orderTitle = "orderTitle")
            )
        }.andExpect {
            status { isOk() }
            content { jsonPath("$.payUserId", equalTo("paymentUserId")) }
            content { jsonPath("$.amount", equalTo(100)) }
            content { jsonPath("$.transactionId", equalTo("transactionId")) }
        }.andDo { print() }

    }

}