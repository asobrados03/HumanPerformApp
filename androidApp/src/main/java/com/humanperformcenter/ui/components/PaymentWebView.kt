package com.humanperformcenter.ui.components

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentWebView(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                var handled = false


                webViewClient = object : WebViewClient() {

                    // Por si en el futuro usas redirecciones/params:
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val targetUrl = request?.url.toString()
                        if (targetUrl.contains("/payments/hpp-response")) {
                            // Mantén este fallback por si algún día mandas RESULT en la URL
                            if (!handled && targetUrl.contains("RESULT=00")) {
                                handled = true
                                onPaymentSuccess()
                                return true
                            } else if (!handled) {
                                handled = true
                                onPaymentCancelled()
                                return true
                            }
                        }
                        return false
                    }

                    override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                        super.onPageFinished(view, finishedUrl)
                        view?.evaluateJavascript(
                            "(function(){return document.body?document.body.innerText:''})()"
                        ) { bodyTextRaw ->
                            if (handled) return@evaluateJavascript
                            val bodyText = bodyTextRaw
                                ?.removePrefix("\"")
                                ?.removeSuffix("\"")
                                ?.replace("\\n", "\n")
                                ?: ""

                            when {
                                bodyText.contains("Pago aprobado", ignoreCase = true) ||
                                        bodyText.contains("aprobado", ignoreCase = true) ||
                                        bodyText.contains("✅") -> {
                                    handled = true
                                    onPaymentSuccess()
                                }
                                bodyText.contains("Pago fallido", ignoreCase = true) ||
                                        bodyText.contains("cancelado", ignoreCase = true) ||
                                        bodyText.contains("problem connecting back", ignoreCase = true) ||
                                        bodyText.contains("❌") -> {
                                    handled = true
                                    onPaymentCancelled()
                                }
                            }
                        }
                    }
                }

                loadDataWithBaseURL(null, url, "text/html", "utf-8", null)
            }
        },
        modifier = modifier
    )
}




