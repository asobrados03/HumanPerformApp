package com.humanperformcenter.shared.data.network


import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.model.PaymentUrlResponse
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

object PaymentApi {
    suspend fun initiatePayment(
        customerId: Int,
        productId: Int,
        billingStreet: String,
        billingPostal: String,
        email: String
    ): PaymentUrlResponse {
        val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/payments/initiate") {
            contentType(ContentType.Application.Json)
            setBody(
                PaymentRequest(
                    customer_id = customerId,
                    product_id = productId,
                    billing_street = billingStreet,
                    billing_postal = billingPostal,
                    email = email
                )
            )

        }

        return response.body()
    }
}
