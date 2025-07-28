package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository
import com.humanperformcenter.shared.data.model.PaymentUrlResponse
import com.humanperformcenter.shared.data.network.ApiClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

object PaymentRepositoryImpl/*: PaymentRepository*/ {
    /*override*/ suspend fun generatePaymentUrl(request: PaymentRequest): String {
        val client = ApiClient.apiClient

        val response: HttpResponse = client.post("${ApiClient.baseUrl}/payments/initiate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.bodyAsText()
        return Json.decodeFromString<PaymentUrlResponse>(body).paymentUrl
    }
}
