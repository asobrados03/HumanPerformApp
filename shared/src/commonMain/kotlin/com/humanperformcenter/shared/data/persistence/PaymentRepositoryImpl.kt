package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository
import com.humanperformcenter.shared.data.network.ApiClient
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
            contentType(ContentType.Application.Json)
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Error al generar la URL de pago: ${response.status}")
        }

        return response.bodyAsText()
    }

    override suspend fun requestGooglePay(requestJson: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean {
        TODO("Not yet implemented")
    }
}
