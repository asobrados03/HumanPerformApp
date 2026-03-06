package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GetStripeCustomerResponse(
    val success: Boolean,
    val data: StripeCustomer
)

@Serializable
data class StripeCustomer(
    val id: String,
    val `object`: String = "customer",
    val address: JsonElement? = null,
    val balance: Long = 0,
    val created: Long,
    val currency: String? = null,
    @SerialName("default_source")
    val defaultSource: JsonElement? = null,
    val delinquent: Boolean = false,
    val description: String? = null,
    val discount: JsonElement? = null,
    val email: String? = null,
    @SerialName("invoice_prefix")
    val invoicePrefix: String? = null,
    @SerialName("invoice_settings")
    val invoiceSettings: InvoiceSettings = InvoiceSettings(),
    val livemode: Boolean = false,
    val metadata: Map<String, String> = emptyMap(),
    val name: String? = null,
    val phone: String? = null,
    @SerialName("preferred_locales")
    val preferredLocales: List<String> = emptyList(),
    val shipping: JsonElement? = null,
    @SerialName("tax_exempt")
    val taxExempt: String = "none",    // "none" | "exempt" | "reverse"
    @SerialName("test_clock")
    val testClock: JsonElement? = null
)

@Serializable
data class InvoiceSettings(
    @SerialName("custom_fields")
    val customFields: JsonElement? = null,
    @SerialName("default_payment_method")
    val defaultPaymentMethod: JsonElement? = null,
    val footer: String? = null,
    @SerialName("rendering_options")
    val renderingOptions: JsonElement? = null
)
