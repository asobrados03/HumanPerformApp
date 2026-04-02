package com.humanperformcenter.shared.integration

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.persistence.StripeRepositoryImpl
import com.humanperformcenter.shared.data.remote.implementation.StripeRemoteDataSourceImpl
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import integration.integrationProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StripeFlowIntegrationTest {

    @Test
    fun publishable_key_customer_setup_payment_refund_and_subscription_flow() = runTest {
        val apiEngine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Get if request.url.encodedPath == "/stripe/publishable-key" -> respond(
                    """{"publishableKey":" pk_test_integration "}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Post if request.url.encodedPath == "/stripe/customer" -> respond(
                    """{"success":true,"data":{"customerId":"cus_123","isNew":true}}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Post if request.url.encodedPath == "/stripe/payments/setup-config" -> respond(
                    """{"success":true,"data":{"customer_id":"cus_123","setup_intent_client_secret":"seti_secret","ephemeral_key":"eph_key"}}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Post if request.url.encodedPath == "/stripe/payment-intents" -> respond(
                    """{"success":true,"data":{"id":"pi_1","amount":1999,"currency":"eur","client_secret":"pi_secret","status":"requires_confirmation","created":1710000000}}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Post if request.url.encodedPath == "/stripe/refund" -> respond(
                    "",
                    HttpStatusCode.OK,
                )
                HttpMethod.Post if request.url.encodedPath == "/stripe/subscription" -> respond(
                    """{"subscription_id":"sub_777","client_secret":"sub_secret","customer_id":"cus_123"}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                else -> error("Unhandled api endpoint: ${request.method} ${request.url}")
            }
        }

        val useCase = StripeUseCase(
            StripeRepositoryImpl(StripeRemoteDataSourceImpl(integrationProvider(apiEngine = apiEngine))),
        )

        val key = useCase.getPublishableKey()
        val customer = useCase.createOrGetCustomer()
        val setup = useCase.createSetupConfig(userId = 22)
        val paymentIntent = useCase.createPaymentIntent(
            CreatePaymentIntentRequest(
                amount = 19.99,
                currency = "eur",
                customerId = "cus_123",
                paymentMethodId = "pm_123",
            ),
        )
        val refund = useCase.createRefund(paymentIntentId = "pi_1", amount = 19.99)
        val subscription = useCase.createSubscription(
            priceId = "price_abc",
            userId = 22,
            productId = 31,
            couponCode = null,
        )

        assertTrue(key.isSuccess)
        assertEquals("pk_test_integration", key.getOrThrow())
        assertTrue(customer.isSuccess)
        assertEquals("cus_123", customer.getOrThrow().data?.customerId)
        assertTrue(setup.isSuccess)
        assertEquals("seti_secret", setup.getOrThrow().data?.clientSecret)
        assertTrue(paymentIntent.isSuccess)
        assertEquals("pi_1", paymentIntent.getOrThrow().data?.id)
        assertTrue(refund.isSuccess)
        assertTrue(subscription.isSuccess)
        assertEquals("sub_777", subscription.getOrThrow().subscriptionId)
    }
}
