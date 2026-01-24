package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.PaymentMethod
import com.humanperformcenter.shared.data.model.payment.PaymentRequest
import com.humanperformcenter.shared.data.model.payment.RebillRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository
import com.humanperformcenter.shared.data.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object PaymentRepositoryImpl: PaymentRepository{
    override suspend fun generatePaymentUrl(request: PaymentRequest): String {
        val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/payments/hpp-url") {
            parameter("amount", request.amount)
            parameter("currency", request.currency)
            parameter("firstName", request.firstName)
            parameter("lastName", request.lastName)
            parameter("email", request.email)
            parameter("street", request.street)
            parameter("city", request.city ?: "")
            parameter("postalCode", request.postalCode ?: 0)
            parameter("card_storage", request.card_storage ?: false)
            parameter("payer_ref", request.payer_ref ?: "")
            parameter("select_stored_cards", request.show_stored ?: false)
            parameter("subscribe", request.subscribe ?: false)
            parameter("product_id", request.product_id ?: 0)
            parameter("interval_months", request.interval_months ?: 1)
            contentType(ContentType.Application.Json)
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Error al generar la URL de pago: ${response.status}")
        }

        return response.bodyAsText()
    }

    override suspend fun getPaymentMethods(userId: Int): List<PaymentMethod> =
        ApiClient.apiClient.get("${ApiClient.baseUrl}/payments/methods") {
            parameter("user_id", userId)
            accept(ContentType.Application.Json)
        }.body()

    override fun getAllowedPaymentMethods(): String {
        TODO("Not yet implemented")
    }

    override fun buildPaymentRequestJson(precio: Double): String {
        TODO("Not yet implemented")
    }

    override suspend fun getPaymentMethod(userId: Int): String? {
        val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/user/saved-payment-method") {
            parameter("user_id", userId)
            contentType(ContentType.Application.Json)
        }

        if (!response.status.isSuccess()) {
            return null
        }
        val token = response.body<String>().trim()
        return token.ifBlank { null }
    }

    override suspend fun rebillPayment(
        rebillRequest: RebillRequest
    ): Boolean {
        val response: HttpResponse = ApiClient.apiClient.post("${ApiClient.baseUrl}/payments/rebill") {
            contentType(ContentType.Application.Json)
            setBody(rebillRequest)
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Error al procesar el rebill: ${response.status}")
        }
        return response.body()
    }
    override suspend fun requestGooglePay(requestJson: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean {
        TODO("Not yet implemented")
    }
}
