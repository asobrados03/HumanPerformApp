package com.humanperformcenter.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethod(
    val id: Long,
    val provider: String,
    @SerialName("payer_ref") val payerRef: String,
    @SerialName("pmt_ref") val pmtRef: String,   // <-- token PMT_REF para rebill
    val brand: String? = null,                   // VISA/MC…
    val last4: String? = null,
    @SerialName("exp_month") val expMonth: Int? = null,
    @SerialName("exp_year") val expYear: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false
)
