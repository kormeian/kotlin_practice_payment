package com.example.kotlin_practice_payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableFeignClients
@EnableJpaAuditing
@Configuration
class PaymentApplicationConfiguration

@SpringBootApplication
class KotlinPracticePaymentApplication

fun main(args: Array<String>) {
    runApplication<KotlinPracticePaymentApplication>(*args)
}
