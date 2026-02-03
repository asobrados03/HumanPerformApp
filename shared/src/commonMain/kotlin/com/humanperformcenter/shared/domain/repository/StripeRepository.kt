package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.*


interface StripeRepository {
    // ==================== CLIENTES ====================
    suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse>
    suspend fun getCustomer(customerId: String): Result<GetStripeCustomerResponse>

    // ==================== EPHEMERAL KEYS ====================
    // Necesario para que el PaymentSheet muestre tarjetas guardadas de forma segura
    suspend fun createEphemeralKey(customerId: String, apiVersion: String): Result<EphemeralKeyDto>

    // ==================== PAYMENT METHODS ====================
    suspend fun attachPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit>
    suspend fun listPaymentMethods(customerId: String): Result<List<PaymentMethodDto>>
    suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit>

    // ==================== PAYMENT INTENTS ====================
    suspend fun createPaymentIntent(createPaymentIntentRequest: CreatePaymentIntentRequest): Result<CreatePiDto>
    suspend fun confirmPaymentIntent(id: String): Result<Unit>
    suspend fun cancelPaymentIntent(id: String): Result<Unit>
    suspend fun getPaymentIntent(id: String): Result<CreatePiDto>

    // ==================== COMPRA DE PRODUCTO ====================
    // Flujo simplificado: cobro directo si ya existe un método de pago
    suspend fun purchaseProduct(productId: Int, paymentMethodId: String): Result<Unit>

    // ==================== REEMBOLSOS ====================
    suspend fun createRefund(paymentIntentId: String, amount: Int?): Result<Unit>

    // ==================== SUSCRIPCIONES ====================
    suspend fun createSubscription(priceId: String): Result<SubscriptionDto>
    suspend fun cancelSubscription(id: String): Result<Unit>
    suspend fun getSubscription(id: String): Result<SubscriptionDto>

    // ==================== TRANSACCIONES ====================
    suspend fun getUserTransactions(): Result<List<TransactionDto>>

    // ==================== TARJETAS (Gestión de Usuario) ====================
    suspend fun saveCard(paymentMethodId: String): Result<Unit>
    suspend fun getUserCards(): Result<List<CardDto>>
    suspend fun deleteCard(cardId: String): Result<Unit>
    suspend fun setDefaultCard(cardId: String): Result<Unit>
}