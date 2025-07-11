package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository
import com.humanperformcenter.shared.domain.security.Crypto_Pays
import com.humanperformcenter.shared.data.model.EncryptedResult
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.*
import io.ktor.http.*

object PaymentRepositoryImpl : PaymentRepository {

    private lateinit var httpClient: HttpClient
    private lateinit var merchantId: String
    private lateinit var productId: String
    private lateinit var sharedSecret: String
    private lateinit var baseUrl: String

    fun init(
        httpClient: HttpClient,
        merchantId: String,
        productId: String,
        sharedSecret: String,
        baseUrl: String
    ) {
        this.httpClient = httpClient
        this.merchantId = merchantId
        this.productId = productId
        this.sharedSecret = sharedSecret
        this.baseUrl = baseUrl
    }

    override suspend fun generatePaymentUrl(request: PaymentRequest): String {
        val params = mapOf(
            "merchantId" to merchantId,
            "productId" to productId,
            "merchantTransactionId" to request.transactionId,
            "amount" to request.amount,
            "currency" to request.currency,
            "country" to request.country,
            "paymentSolution" to "creditcards",
            "customerId" to request.customerId,
            "customerEmail" to request.customerEmail,
            "successURL" to request.successUrl,
            "errorURL" to request.errorUrl,
            "cancelURL" to request.cancelUrl,
            "statusURL" to request.statusUrl
        )

        val paramString = params.entries.joinToString("&") { "${it.key}=${it.value}" }

        val crypto = Crypto_Pays
        val integrityCheck = crypto.sha256(paramString)
        val encrypted: EncryptedResult = crypto.encryptAES(paramString, sharedSecret)

        val response: HttpResponse = httpClient.submitForm(
            url = "$baseUrl/EPGCheckout/rest/online/tokenize",
            formParameters = Parameters.build {
                append("merchantId", merchantId)
                append("encrypted", encrypted.encrypted)
                append("integrityCheck", integrityCheck)
            },
            encodeInQuery = false
        ) {
            headers {
                append("apiVersion", "5")
                append("encryptionMode", "CBC")
                append("iv", encrypted.iv)
            }
        }

        return response.bodyAsText()
    }
}
