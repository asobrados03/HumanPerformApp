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

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val targetUrl = request?.url.toString()
                        if (targetUrl.contains("/payments/hpp-response")) {
                            if (targetUrl.contains("RESULT=00")) {
                                onPaymentSuccess()
                            } else {
                                onPaymentCancelled()
                            }
                            return true
                        }
                        return false
                    }
                }
                loadDataWithBaseURL(null, url, "text/html", "utf-8", null)
            }
        },
        modifier = modifier
    )
}




