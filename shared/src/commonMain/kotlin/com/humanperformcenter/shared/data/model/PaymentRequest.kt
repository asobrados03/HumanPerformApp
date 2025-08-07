package com.humanperformcenter.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val amount: Int,
    val currency: String
    /*
    val firstName: String = "Cesar",
    val lastName: String = "Tester",
    val email: String = "cesar@example.com",
    val street: String = "Calle Ficticia 123",
    val postalCode: String = "28001",
    val city: String = "Madrid",
    val countryCode: String = "724"
     */
)