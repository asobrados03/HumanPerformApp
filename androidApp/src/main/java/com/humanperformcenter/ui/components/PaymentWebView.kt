package com.humanperformcenter.ui.components

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentWebView(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentCancelled: () -> Unit
) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = settings.userAgentString + " AndroidApp"
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val targetUrl = request?.url?.toString() ?: return false
                    return handleRedirect(targetUrl, onPaymentSuccess, onPaymentCancelled)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return url?.let {
                        handleRedirect(it, onPaymentSuccess, onPaymentCancelled)
                    } ?: false
                }
            }
            loadUrl(url)
        }
    })
}

private fun handleRedirect(
    targetUrl: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
): Boolean {
    return when {
        targetUrl.contains("pago-ok", ignoreCase = true) -> {
            onSuccess()
            true
        }
        targetUrl.contains("pago-error", ignoreCase = true) ||
                targetUrl.contains("pago-cancelado", ignoreCase = true) -> {
            onCancel()
            true
        }
        else -> false
    }
}


