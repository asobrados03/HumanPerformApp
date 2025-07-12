package com.humanperformcenter.android.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PaymentWebView(
    url: String,
    onResult: (Boolean) -> Unit
) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                    val destinationUrl = request?.url.toString()
                    when {
                        destinationUrl.startsWith("https://miapp.com/pago_ok") -> {
                            onResult(true)
                            return true
                        }
                        destinationUrl.startsWith("https://miapp.com/pago_error") ||
                                destinationUrl.startsWith("https://miapp.com/pago_cancel") -> {
                            onResult(false)
                            return true
                        }
                        else -> return false
                    }
                }
            }

            loadUrl(url)
        }
    })
}
