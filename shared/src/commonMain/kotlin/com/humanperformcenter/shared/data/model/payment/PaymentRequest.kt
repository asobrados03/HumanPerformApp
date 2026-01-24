package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val amount: Int,
    val currency: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val street: String? = null,
    val postalCode: Int? = 0,
    val city: String? = "",
    val card_storage: Boolean? = false,
    val payer_ref: String? = null,
    val show_stored : Boolean? = true,
    val subscribe : Boolean? = false,
    val product_id: Int? = null,
    val interval_months: Int? = null
)