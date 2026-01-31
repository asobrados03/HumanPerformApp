package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.PaymentMethod
import com.humanperformcenter.shared.data.model.payment.PaymentRequest
import com.humanperformcenter.shared.data.model.payment.RebillRequest
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.PaymentRepository

class PaymentUseCase(private val paymentRepository: PaymentRepository) {
    suspend fun generatePaymentUrl(request: PaymentRequest): String {
        return paymentRepository.generatePaymentUrl(request)
    }

    suspend fun requestGooglePay(requestJson: String): String {
        return paymentRepository.requestGooglePay(requestJson)
    }

    suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean {
        return paymentRepository.sendTokenToBackend(token, amount, currency)
    }

    suspend fun getPaymentMethod(userId: Int): String? {
        return paymentRepository.getPaymentMethod(userId)
    }

    suspend fun rebillPayment(rebillRequest: RebillRequest): Boolean {
        return paymentRepository.rebillPayment(rebillRequest)
    }

    suspend fun getPaymentMethods(userId: Int): List<PaymentMethod> {
        return paymentRepository.getPaymentMethods(userId)
    }

    fun createHppPaymentRequest(
        product: Product,
        user: User?,
        showStored: Boolean,
        saveCard: Boolean
    ): PaymentRequest {
        // Extraer lógica de nombres, direcciones, etc.
        val firstName = user?.fullName?.split(" ")?.firstOrNull() ?: "Usuario"
        val lastName = user?.fullName?.split(" ")?.drop(1)?.joinToString(" ") ?: ""

        return PaymentRequest(
            amount = 1, // OJO: Revisar si esto es 1 siempre o el precio real
            currency = "EUR",
            firstName = firstName,
            lastName = lastName,
            email = user?.email ?: "",
            street = user?.postAddress ?: "",
            postalCode = user?.postcode ?: 0,
            city = "Segovia", // Hardcoded, considerar sacar de user o config
            card_storage = saveCard,
            payer_ref = user?.let { "user_${it.id}" },
            show_stored = showStored,
            product_id = product.id,
            interval_months = if (product.typeOfProduct == "recurrent") 1 else null,
            subscribe = product.typeOfProduct == "recurrent"
        )
    }
}