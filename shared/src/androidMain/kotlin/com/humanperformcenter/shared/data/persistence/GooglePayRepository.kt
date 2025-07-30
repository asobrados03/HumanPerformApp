package com.humanperformcenter.shared.data.persistence

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.PaymentRepository
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object GooglePayRepository : PaymentRepository {
    private const val REQUEST_CODE = 9001

    private lateinit var activity: ComponentActivity
    private val paymentsClient by lazy {
        Wallet.getPaymentsClient(
            activity,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build()
        )
    }

    private var cont: Continuation<String>? = null

    /** Debe llamarse una vez desde tu Activity principal */
    fun init(activity: ComponentActivity) {
        this.activity = activity
        // Al acceder a paymentsClient se inicializará con ese activity
    }

    /** 1) Lanza Google Pay y devuelve el token */
    override suspend fun requestGooglePay(requestJson: String): String =
        suspendCancellableCoroutine { continuation ->
            cont = continuation
            val paymentRequest = PaymentDataRequest.fromJson(requestJson)
            val task = paymentsClient.loadPaymentData(paymentRequest)

            // Listener éxito
            val successListener = { paymentData: PaymentData? ->
                try {
                    val token = JSONObject(paymentData!!.toJson())
                        .getJSONObject("paymentMethodData")
                        .getJSONObject("tokenizationData")
                        .getString("token")
                    continuation.resume(token)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }

            // Listener error
            val failureListener = { exception: Exception ->
                continuation.resumeWithException(exception)
            }

            task.addOnSuccessListener(successListener)
            task.addOnFailureListener(failureListener)
        }

    /** 2) Envía el token al backend (Addon Payments) */
    override suspend fun sendTokenToBackend(token: String): Boolean {
        val response: HttpResponse = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/pago") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("token" to token))
        }
        return response.status.isSuccess()
    }

    /** 3) (Opcional) Genera una URL de pago en tu backend */
    override suspend fun generatePaymentUrl(request: PaymentRequest): String {
        val response: HttpResponse = ApiClient.apiClient.post("${ApiClient.baseUrl}/payments/initiate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        // Asume que tu backend te devuelve { "url": "https://..." }
        val json = response.body<Map<String, String>>()
        return json["url"] ?: throw Exception("No se recibió URL de pago")
    }

    /** 4) Debe llamarse desde onActivityResult (o ActivityResultCallback) */
    fun handleGooglePayResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE) return
        when (resultCode) {
            Activity.RESULT_OK -> {
                val paymentData = PaymentData.getFromIntent(data!!)
                val token = JSONObject(paymentData?.toJson())
                    .getJSONObject("paymentMethodData")
                    .getJSONObject("tokenizationData")
                    .getString("token")
                cont?.resume(token)
            }
            Activity.RESULT_CANCELED ->
                cont?.resumeWithException(CancellationException("Pago cancelado"))
            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(data)
                cont?.resumeWithException(Exception("Error GPay: ${status?.statusCode}"))
            }
        }
        cont = null
    }
}
